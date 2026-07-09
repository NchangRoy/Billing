package com.example.account.modules.facturation.service;

import com.example.account.modules.facturation.dto.request.QuotationProposalCreateRequest;
import com.example.account.modules.facturation.dto.request.QuotationProposalLineRequest;
import com.example.account.modules.facturation.dto.response.QuotationProposalResponse;
import com.example.account.modules.facturation.model.entity.LigneQuotationProposal;
import com.example.account.modules.facturation.model.entity.QuotationProposal;
import com.example.account.modules.facturation.model.enums.StatutQuotationProposal;
import com.example.account.modules.facturation.repository.QuotationProposalRepository;
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

        return entityTemplate.insert(proposal).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<QuotationProposalResponse> getAll() {
        return repository.findAll().map(this::toResponse);
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
