package com.example.account.modules.facturation.service;

import com.example.account.modules.facturation.adapter.output.persistence.SellerPersistenceEntity;
import com.example.account.modules.facturation.dto.request.QuotationProposalCreateRequest;
import com.example.account.modules.facturation.dto.request.QuotationProposalLineRequest;
import com.example.account.modules.facturation.dto.response.QuotationProposalResponse;
import com.example.account.modules.facturation.model.entity.CustomerAssignment;
import com.example.account.modules.facturation.model.entity.LigneQuotationProposal;
import com.example.account.modules.facturation.model.entity.QuotationProposal;
import com.example.account.modules.facturation.model.entity.SellerAgencyAssignment;
import com.example.account.modules.facturation.model.enums.SellerRole;
import com.example.account.modules.facturation.model.enums.StatutQuotationProposal;
import com.example.account.modules.facturation.repository.CustomerAssignmentRepository;
import com.example.account.modules.facturation.repository.QuotationProposalRepository;
import com.example.account.modules.facturation.repository.SellerAgencyAssignmentRepository;
import com.example.account.modules.tiers.domain.port.input.ThirdPartyContactUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotationProposalService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final QuotationProposalRepository repository;
    private final R2dbcEntityTemplate entityTemplate;
    private final com.example.account.modules.facturation.service.SellerLocalService sellerLocalService;
    private final SellerAgencyAssignmentRepository sellerAgencyAssignmentRepository;
    private final CustomerAssignmentRepository customerAssignmentRepository;
    private final ThirdPartyContactUseCase thirdPartyContactUseCase;

    // Same reasoning as KernelClientAdapter.ENRICHMENT_CONCURRENCY — capping
    // how many actorId resolutions fire against Kernel at once per scoping
    // request, so a seller/agency with many assigned clients doesn't burst
    // kernel-core with simultaneous connections.
    private static final int ACTOR_RESOLUTION_CONCURRENCY = 3;

    @Transactional
    public Mono<QuotationProposalResponse> create(QuotationProposalCreateRequest request) {
        log.info("Client {} is proposing a quotation for org {}", request.getIdClient(), request.getOrganizationId());

        LocalDateTime now = LocalDateTime.now();
        QuotationProposal proposal = QuotationProposal.builder()
                .idProposal(UUID.randomUUID())
                .numeroProposal(generateNumero())
                .organizationId(request.getOrganizationId())
                .idClient(request.getIdClient())
                .nomClient(request.getNomClient())
                .emailClient(request.getEmailClient())
                .dateCreation(now)
                .dateValidite(request.getDateValidite())
                .statut(StatutQuotationProposal.BROUILLON)
                .commentary(request.getCommentary())
                .lignesProposal(toLines(request.getLignesProposal()))
                .montantHT(request.getMontantHT())
                .montantTVA(request.getMontantTVA())
                .montantTTC(request.getMontantTTC())
                .applyVat(request.getApplyVat() != null ? request.getApplyVat() : true)
                .devise(request.getDevise() != null ? request.getDevise() : "XAF")
                .createdAt(now)
                .updatedAt(now)
                .build();

        return withActorId(proposal)
                .flatMap(entityTemplate::insert)
                .map(this::toResponse);
    }

    /**
     * Stamps the client's Kernel actor id onto the proposal at creation time
     * so seller-assignment scoping can join on it instead of idClient — see
     * DevisUseCaseImpl.withActorId for the full reasoning.
     */
    private Mono<QuotationProposal> withActorId(QuotationProposal proposal) {
        if (proposal.getIdClient() == null) {
            return Mono.just(proposal);
        }
        return thirdPartyContactUseCase.resolveActorId(proposal.getIdClient())
                .doOnNext(proposal::setActorId)
                .onErrorResume(e -> {
                    log.warn("Could not resolve actor id for client {} (continuing without it): {}", proposal.getIdClient(), e.getMessage());
                    return Mono.empty();
                })
                .thenReturn(proposal);
    }

    @Transactional(readOnly = true)
    public Mono<QuotationProposalResponse> getById(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found: " + id)))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<QuotationProposalResponse> getByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(this::toResponse);
    }

    /**
     * Scopes the proposal list to what the caller is actually allowed to see:
     * a proposal has no sellerId of its own (see QuotationProposal), only an
     * idClient — visibility is derived by joining through CustomerAssignment
     * (which customer is whose) same as CustomerAssignmentService already does
     * for the "my customers" views.
     * <p>
     * sellerId is caller-supplied (same trust model as Sessions' own sellerId
     * filter elsewhere in this API — there's no caller identity in the Kernel
     * access token itself to resolve this from, see AuthServiceAdapter), but
     * the ROLE used to decide the scope is always looked up fresh from our own
     * sellers table, never trusted from the request — a caller can't widen
     * their own visibility by just claiming a different role.
     */
    @Transactional(readOnly = true)
    public Flux<QuotationProposalResponse> getScopedForSeller(UUID organizationId, UUID sellerId) {
        return sellerLocalService.findById(sellerId)
                .filter(seller -> organizationId.equals(seller.getOrganizationId()))
                .flatMapMany(seller -> scopedProposalsFor(seller, organizationId));
    }

    private Flux<QuotationProposalResponse> scopedProposalsFor(SellerPersistenceEntity seller, UUID organizationId) {
        String role = seller.getRole();
        if (SellerRole.OWNER.name().equals(role)) {
            return getByOrganizationId(organizationId);
        }
        if (SellerRole.AGENCY_MANAGER.name().equals(role)) {
            return sellerAgencyAssignmentRepository.findBySellerId(seller.getId())
                    .flatMapMany(own -> sellerAgencyAssignmentRepository.findByAgencyId(own.getAgencyId()))
                    .map(SellerAgencyAssignment::getSellerId)
                    .collectList()
                    .flatMapMany(sellerIds -> proposalsAssignedTo(sellerIds, organizationId));
        }
        // SELLER / POS_SELLER: only the customers assigned to this one seller.
        return proposalsAssignedTo(List.of(seller.getId()), organizationId);
    }

    private Flux<QuotationProposalResponse> proposalsAssignedTo(List<UUID> sellerIds, UUID organizationId) {
        if (sellerIds.isEmpty()) {
            return Flux.empty();
        }
        return customerAssignmentRepository.findBySellerIdInAndOrganizationId(sellerIds, organizationId)
                .map(CustomerAssignment::getClientId)
                .distinct()
                .flatMap(clientId -> thirdPartyContactUseCase.resolveActorId(clientId)
                        .onErrorResume(e -> {
                            log.warn("Could not resolve actor id for client {} (skipping): {}", clientId, e.getMessage());
                            return Mono.empty();
                        }), ACTOR_RESOLUTION_CONCURRENCY)
                .collectList()
                .flatMapMany(actorIds -> actorIds.isEmpty()
                        ? Flux.empty()
                        : repository.findByActorIdInAndOrganizationId(actorIds, organizationId).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public Flux<QuotationProposalResponse> getByClientAndOrganization(UUID idClient, UUID organizationId) {
        return repository.findByIdClientAndOrganizationId(idClient, organizationId).map(this::toResponse);
    }

    @Transactional
    public Mono<QuotationProposalResponse> accept(UUID id, String commentary) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found: " + id)))
                .flatMap(proposal -> {
                    proposal.setStatut(StatutQuotationProposal.ACCEPTED);
                    if (commentary != null && !commentary.isBlank()) {
                        proposal.setCommentary(commentary);
                    }
                    proposal.setUpdatedAt(LocalDateTime.now());
                    return repository.save(proposal);
                })
                .map(this::toResponse);
    }

    @Transactional
    public Mono<QuotationProposalResponse> reject(UUID id, String commentary) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Proposal not found: " + id)))
                .flatMap(proposal -> {
                    proposal.setStatut(StatutQuotationProposal.REJECTED);
                    if (commentary != null && !commentary.isBlank()) {
                        proposal.setCommentary(commentary);
                    }
                    proposal.setUpdatedAt(LocalDateTime.now());
                    return repository.save(proposal);
                })
                .map(this::toResponse);
    }

    private List<LigneQuotationProposal> toLines(List<QuotationProposalLineRequest> lines) {
        if (lines == null) return List.of();
        return lines.stream().map(l -> LigneQuotationProposal.builder()
                .idProduit(l.getIdProduit())
                .nomProduit(l.getNomProduit())
                .saleSize(l.getSaleSize())
                .quantite(l.getQuantite())
                .prixUnitaire(l.getPrixUnitaire())
                .montantTotal(l.getMontantTotal())
                .build()).collect(Collectors.toList());
    }

    private String generateNumero() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int suffix = 1000 + RANDOM.nextInt(9000);
        return "PROP-" + datePart + "-" + suffix;
    }

    private QuotationProposalResponse toResponse(QuotationProposal p) {
        return QuotationProposalResponse.builder()
                .idProposal(p.getIdProposal())
                .numeroProposal(p.getNumeroProposal())
                .organizationId(p.getOrganizationId())
                .idClient(p.getIdClient())
                .nomClient(p.getNomClient())
                .emailClient(p.getEmailClient())
                .dateCreation(p.getDateCreation())
                .dateValidite(p.getDateValidite())
                .statut(p.getStatut())
                .commentary(p.getCommentary())
                .lignesProposal(p.getLignesProposal())
                .montantHT(p.getMontantHT())
                .montantTVA(p.getMontantTVA())
                .montantTTC(p.getMontantTTC())
                .applyVat(p.getApplyVat())
                .devise(p.getDevise())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
