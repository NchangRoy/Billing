package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.model.FactureProforma;
import com.example.account.modules.facturation.domain.port.input.FactureProformaUseCase;
import com.example.account.modules.facturation.domain.port.output.FactureProformaRepositoryPort;
import com.example.account.modules.facturation.dto.request.ProformaInvoiceRequest;
import com.example.account.modules.facturation.dto.response.ProformaInvoiceResponse;
import com.example.account.modules.facturation.mapper.FactureProformaMapper;
import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.facturation.model.enums.StatutProforma;
import com.example.account.modules.facturation.dto.request.AssignDocPermissionRequest;
import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;
import com.example.account.modules.facturation.service.DocPermissionService;
import com.example.account.modules.facturation.service.EmailService;
import com.example.account.modules.tiers.domain.port.input.ClientUseCase;
import com.example.account.modules.tiers.domain.port.input.ThirdPartyContactUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactureProformaUseCaseImpl implements FactureProformaUseCase {

    private final FactureProformaRepositoryPort proformaRepository;
    private final FactureProformaMapper proformaMapper;
    private final DocPermissionService docPermissionService;
    private final EmailService emailService;
    private final ClientUseCase clientUseCase;
    private final ThirdPartyContactUseCase thirdPartyContactUseCase;

    @Value("${client-portal.frontend-url}")
    private String portalFrontendUrl;

    /**
     * Stamps the client's Kernel actor id onto the proforma at creation time
     * — see DevisUseCaseImpl.withActorId for the full reasoning.
     */
    private Mono<FactureProforma> withActorId(FactureProforma proforma) {
        if (proforma.getIdClient() == null) {
            return Mono.just(proforma);
        }
        return thirdPartyContactUseCase.resolveActorId(proforma.getIdClient())
                .doOnNext(proforma::setActorId)
                .onErrorResume(e -> {
                    log.warn("Could not resolve actor id for client {} (continuing without it): {}", proforma.getIdClient(), e.getMessage());
                    return Mono.empty();
                })
                .thenReturn(proforma);
    }

    private <T> Mono<T> grantOwnerPermission(UUID sellerId, UUID docId, T response) {
        if (sellerId == null || docId == null) return Mono.just(response);
        AssignDocPermissionRequest request = new AssignDocPermissionRequest();
        request.setSellerId(sellerId);
        request.setDocId(docId);
        request.setDocType(DocType.FACTURE_PROFORMA);
        request.setPermission(DocPermissionLevel.OWNER);
        return docPermissionService.grant(request)
                .thenReturn(response)
                .onErrorResume(e -> {
                    log.error("Failed to grant owner doc-permission for proforma {}: {}", docId, e.getMessage());
                    return Mono.just(response);
                });
    }

    @Override
    @Transactional
    public Mono<ProformaInvoiceResponse> createProforma(ProformaInvoiceRequest request) {
        log.info("Création d'une nouvelle facture proforma pour le client: {}", request.getIdClient());

        FactureProforma proforma = proformaMapper.toEntity(request);
        if (proforma.getIdFactureProforma() == null) {
            proforma.setIdFactureProforma(UUID.randomUUID());
        }
        proforma.setDateCreation(LocalDateTime.now());

        if (proforma.getStatut() == null) {
            proforma.setStatut(StatutProforma.BROUILLON);
        }

        return ReactiveOrganizationContext.getOrganizationId()
                .flatMap(orgId -> {
                    proforma.setOrganizationId(orgId);
                    return withActorId(proforma);
                })
                .flatMap(proformaRepository::insert)
                .flatMap(savedProforma -> grantOwnerPermission(
                        savedProforma.getCreatedBy(),
                        savedProforma.getIdFactureProforma(),
                        proformaMapper.toResponse(savedProforma)));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ProformaInvoiceResponse> getProformaById(UUID id) {
        log.info("Récupération de la facture proforma: {}", id);
        return proformaRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture proforma non trouvée: " + id)))
                .map(proformaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<ProformaInvoiceResponse> getAllProformas() {
        log.info("Récupération de toutes les factures proforma");
        return proformaRepository.findAll()
                .map(proformaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<ProformaInvoiceResponse> getProformasByClient(UUID idClient) {
        log.info("Récupération des factures proforma du client: {}", idClient);
        return proformaRepository.findByIdClient(idClient)
                .map(proformaMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> deleteProforma(UUID id) {
        log.info("Suppression de la facture proforma: {}", id);
        return proformaRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Facture proforma non trouvée: " + id));
                    }
                    return proformaRepository.deleteById(id);
                });
    }

    @Override
    @Transactional
    public Mono<ProformaInvoiceResponse> updateStatut(UUID id, StatutProforma nouveauStatut) {
        log.info("Mise à jour du statut de la facture proforma: {} vers {}", id, nouveauStatut);
        return proformaRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture proforma non trouvée: " + id)))
                .flatMap(proforma -> {
                    proforma.setStatut(nouveauStatut);
                    if (nouveauStatut == StatutProforma.ACCEPTE) {
                        proforma.setDateAcceptation(LocalDateTime.now());
                    } else if (nouveauStatut == StatutProforma.REFUSE) {
                        proforma.setDateRefus(LocalDateTime.now());
                    }
                    return proformaRepository.save(proforma);
                })
                .map(proformaMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<ProformaInvoiceResponse> updateFactureProforma(UUID id, ProformaInvoiceRequest request) {
        log.info("Mise à jour de la facture proforma: {}", id);
        return proformaRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture proforma non trouvée: " + id)))
                .flatMap(proforma -> {
                    proformaMapper.updateProformaFromDTO(request, proforma);
                    return proformaRepository.save(proforma);
                })
                .map(proformaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<ProformaInvoiceResponse> getProformasByOrganizationId(UUID organizationId) {
        log.info("Récupération des factures proforma par organisation: {}", organizationId);
        return proformaRepository.findByOrganizationId(organizationId).map(proformaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<ProformaInvoiceResponse> getProformasByAgencyId(UUID agencyId) {
        log.info("Récupération des factures proforma par agence: {}", agencyId);
        return proformaRepository.findByAgencyId(agencyId).map(proformaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<ProformaInvoiceResponse> getProformasBySellerId(UUID sellerId) {
        log.info("Récupération des factures proforma accessibles par le vendeur: {}", sellerId);
        return docPermissionService.findBySellerAndDocType(sellerId, DocType.FACTURE_PROFORMA)
                .flatMap(permission -> proformaRepository.findById(permission.getDocId())
                        .map(proforma -> {
                            ProformaInvoiceResponse response = proformaMapper.toResponse(proforma);
                            response.setDocPermission(docPermissionService.toResponse(permission));
                            return response;
                        }));
    }

    /**
     * Marks the proforma ENVOYE, bootstraps the client's (login-based) portal
     * access only if they've never had it, then emails a "new document"
     * notification — same pattern as DevisUseCaseImpl.sendToPortal. Only
     * fires from BROUILLON: calling this again on an already-sent proforma is
     * a no-op. The "Send to Contact" picker can target any address-book
     * contact instead of the proforma's own stored emailClient — falls back
     * to that stored email when no contact was picked. Status is only
     * persisted once the email has actually gone out, so a failed send never
     * leaves the proforma falsely marked as sent.
     */
    @Override
    @Transactional
    public Mono<Void> sendToPortal(UUID id, com.example.account.modules.facturation.dto.request.ShareDocumentRequest request) {
        log.info("Envoi de la facture proforma {} vers le portail client", id);
        return proformaRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture proforma non trouvée: " + id)))
                .flatMap(proforma -> {
                    if (proforma.getStatut() != StatutProforma.BROUILLON) {
                        log.info("Facture proforma {} déjà envoyée (statut: {}), envoi ignoré", id, proforma.getStatut());
                        return Mono.empty();
                    }
                    String targetEmail = (request != null && request.getContactEmail() != null && !request.getContactEmail().isBlank())
                            ? request.getContactEmail() : proforma.getEmailClient();
                    String targetName = (request != null && request.getContactName() != null && !request.getContactName().isBlank())
                            ? request.getContactName() : proforma.getNomClient();
                    if (targetEmail == null || targetEmail.isBlank()) {
                        return Mono.error(new IllegalStateException("Le client n'a pas d'adresse email renseignée."));
                    }
                    return clientUseCase.ensureClientPortalAccess(proforma.getIdClient(), targetEmail, targetName)
                            .onErrorResume(e -> {
                                log.warn("ensureClientPortalAccess failed for facture proforma {} (continuing anyway): {}", id, e.getMessage());
                                return Mono.empty();
                            })
                            .then(emailService.sendPortalDocumentNotification(
                                    targetEmail, targetName,
                                    "Facture Proforma", proforma.getNumeroProformaInvoice(),
                                    portalFrontendUrl + "/portal/login"))
                            .then(Mono.defer(() -> {
                                proforma.setStatut(StatutProforma.ENVOYE);
                                return proformaRepository.save(proforma);
                            }));
                })
                .then();
    }
}
