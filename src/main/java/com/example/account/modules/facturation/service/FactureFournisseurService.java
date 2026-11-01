package com.example.account.modules.facturation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.account.modules.facturation.dto.request.FactureFournisseurCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureFournisseurResponse;
import com.example.account.modules.facturation.dto.request.AssignDocPermissionRequest;
import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;
import com.example.account.modules.facturation.model.enums.StatutFactureFournisseur;
import com.example.account.modules.facturation.domain.port.output.AccountingServicePort;
import com.example.account.modules.facturation.domain.port.output.FactureFournisseurServicePort;
import com.example.account.modules.tiers.domain.port.input.FournisseurUseCase;
import com.example.account.modules.tiers.domain.port.input.ThirdPartyContactUseCase;

/**
 * FactureFournisseur is now persisted in sales-core; this service is a thin
 * orchestrator on top of FactureFournisseurServicePort (WebClient -> sales-core),
 * keeping only the account-only concerns: doc-permission grants and the
 * permission-aware seller-scoped lookup used by the supplier portal.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FactureFournisseurService {

    private final FactureFournisseurServicePort factureFournisseurServicePort;
    private final DocPermissionService docPermissionService;
    private final AccountingServicePort accountingServicePort;
    private final FournisseurUseCase fournisseurUseCase;
    private final ThirdPartyContactUseCase thirdPartyContactUseCase;
    private final FactureActorIndexService factureActorIndexService;
    private final EmailService emailService;

    @Value("${client-portal.frontend-url}")
    private String portalFrontendUrl;

    public Mono<Void> accountFacture(UUID id) {
        log.info("Comptabilisation de la facture fournisseur: {}", id);
        return accountingServicePort.sendFactureFournisseurData(id)
                .onErrorResume(e -> {
                    log.error("Failed to sync facture fournisseur {} with accounting: {}", id, e.getMessage());
                    return Mono.error(new Exception("Accounting sync failed: " + e.getMessage()));
                });
    }

    public Mono<Void> markAccounted(UUID id) {
        return accountingServicePort.markFactureFournisseurAccounted(id);
    }

    private <T> Mono<T> grantOwnerPermission(UUID sellerId, UUID docId, T response) {
        if (sellerId == null || docId == null) return Mono.just(response);
        AssignDocPermissionRequest request = new AssignDocPermissionRequest();
        request.setSellerId(sellerId);
        request.setDocId(docId);
        request.setDocType(DocType.FACTURE_FOURNISSEUR);
        request.setPermission(DocPermissionLevel.OWNER);
        return docPermissionService.grant(request)
                .thenReturn(response)
                .onErrorResume(e -> {
                    log.error("Failed to grant owner doc-permission for facture fournisseur {}: {}", docId, e.getMessage());
                    return Mono.just(response);
                });
    }

    @Transactional
    public Mono<FactureFournisseurResponse> createFacture(FactureFournisseurCreateRequest dto) {
        log.info("Création d'une nouvelle facture fournisseur");
        return factureFournisseurServicePort.createFacture(dto)
                .flatMap(saved -> recordActorIndex(saved)
                        .then(grantOwnerPermission(saved.getCreatedBy(), saved.getIdFactureFournisseur(), saved)));
    }

    /**
     * FactureFournisseur isn't a local table (see FactureActorIndexEntity) —
     * this populates Billing's own small factureId->actorId index so the
     * client portal can find "my supplier invoices" without a cross-service
     * join.
     */
    private Mono<Void> recordActorIndex(FactureFournisseurResponse saved) {
        if (saved.getIdFactureFournisseur() == null || saved.getIdFournisseur() == null) {
            return Mono.empty();
        }
        return thirdPartyContactUseCase.resolveActorId(saved.getIdFournisseur())
                .flatMap(actorId -> factureActorIndexService.record(saved.getIdFactureFournisseur(), actorId))
                .onErrorResume(e -> {
                    log.warn("Could not resolve actor id for fournisseur {} (continuing without it): {}", saved.getIdFournisseur(), e.getMessage());
                    return Mono.empty();
                });
    }

    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getAllFactures() {
        log.info("Récupération de toutes les factures fournisseur");
        return factureFournisseurServicePort.getAllFactures();
    }

    @Transactional
    public Mono<FactureFournisseurResponse> updateFacture(UUID id, FactureFournisseurCreateRequest dto) {
        log.info("Mise à jour de la facture fournisseur: {}", id);
        return factureFournisseurServicePort.updateFacture(id, dto);
    }

    @Transactional
    public Mono<Void> deleteFacture(UUID id) {
        log.info("Suppression de la facture fournisseur: {}", id);
        return factureFournisseurServicePort.deleteFacture(id);
    }

    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getByOrganizationId(UUID organizationId) {
        return factureFournisseurServicePort.getByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getByAgencyId(UUID agencyId) {
        return factureFournisseurServicePort.getByAgencyId(agencyId);
    }

    /** sales-core has no by-fournisseur endpoint, so this narrows via organisation (the closest scoped query it does offer) instead of fetching every invoice in the system. */
    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getByOrganizationIdAndFournisseurId(UUID organizationId, UUID fournisseurId) {
        return factureFournisseurServicePort.getByOrganizationId(organizationId)
                .filter(f -> fournisseurId.equals(f.getIdFournisseur()));
    }

    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getBySellerId(UUID sellerId) {
        return docPermissionService.findBySellerAndDocType(sellerId, DocType.FACTURE_FOURNISSEUR)
                .flatMap(permission -> factureFournisseurServicePort.findById(permission.getDocId())
                        .map(response -> {
                            response.setDocPermission(docPermissionService.toResponse(permission));
                            return response;
                        }));
    }

    /**
     * Marks the supplier invoice ENVOYE, bootstraps the fournisseur's
     * (login-based) portal access only if they've never had it, then emails
     * a "new document" notification — same pattern as
     * DevisUseCaseImpl.sendToPortal / BonAchatService.sendToPortal. Only
     * fires from BROUILLON: calling this again on an already-sent invoice is
     * a no-op. Status is only persisted once the email has actually gone
     * out, so a failure here never leaves the invoice falsely marked sent.
     */
    @Transactional
    public Mono<Void> sendToPortal(UUID id, com.example.account.modules.facturation.dto.request.ShareDocumentRequest request) {
        log.info("Envoi de la facture fournisseur {} vers le portail fournisseur", id);
        return factureFournisseurServicePort.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture fournisseur non trouvée: " + id)))
                .flatMap(facture -> {
                    if (facture.getStatut() != StatutFactureFournisseur.BROUILLON) {
                        log.info("Facture fournisseur {} déjà envoyée (statut: {}), envoi ignoré", id, facture.getStatut());
                        return Mono.empty();
                    }
                    // Lets the "Send to Contact" picker target any address-book
                    // contact instead of the facture's own stored
                    // emailFournisseur — falls back to that stored email when
                    // no contact was picked.
                    String targetEmail = (request != null && request.getContactEmail() != null && !request.getContactEmail().isBlank())
                            ? request.getContactEmail() : facture.getEmailFournisseur();
                    String targetName = (request != null && request.getContactName() != null && !request.getContactName().isBlank())
                            ? request.getContactName() : facture.getNomFournisseur();
                    if (targetEmail == null || targetEmail.isBlank()) {
                        return Mono.error(new IllegalStateException("Le fournisseur n'a pas d'adresse email renseignée."));
                    }
                    return fournisseurUseCase.ensureFournisseurPortalAccess(
                                    facture.getIdFournisseur(), targetEmail, targetName)
                            // Kernel's ensure-portal-access endpoint isn't deployed yet (404) — this
                            // is a bootstrap-only nicety anyway (grants login to brand-new fournisseurs),
                            // so a failure here shouldn't block the actual notification email.
                            .onErrorResume(e -> {
                                log.warn("ensureFournisseurPortalAccess failed for facture fournisseur {} (continuing anyway): {}", id, e.getMessage());
                                return Mono.empty();
                            })
                            .then(emailService.sendPortalDocumentNotification(
                                    targetEmail, targetName,
                                    "Facture fournisseur", facture.getNumeroFacture(),
                                    portalFrontendUrl + "/portal/login"))
                            .then(Mono.defer(() -> factureFournisseurServicePort.updateFacture(id, toSentUpdateRequest(facture))));
                })
                .then();
    }

    private FactureFournisseurCreateRequest toSentUpdateRequest(FactureFournisseurResponse facture) {
        return FactureFournisseurCreateRequest.builder()
                .numeroFacture(facture.getNumeroFacture())
                .idFournisseur(facture.getIdFournisseur())
                .nomFournisseur(facture.getNomFournisseur())
                .adresseFournisseur(facture.getAdresseFournisseur())
                .emailFournisseur(facture.getEmailFournisseur())
                .telephoneFournisseur(facture.getTelephoneFournisseur())
                .lines(facture.getLines())
                .montantHT(facture.getMontantHT())
                .montantTVA(facture.getMontantTVA())
                .montantTTC(facture.getMontantTTC())
                .montantTotal(facture.getMontantTotal())
                .modeReglement(facture.getModeReglement())
                .nbreEcheance(facture.getNbreEcheance())
                .montantRestant(facture.getMontantRestant())
                .dateFacture(facture.getDateFacture())
                .dateEcheance(facture.getDateEcheance())
                .statut(StatutFactureFournisseur.ENVOYE)
                .applyVat(facture.getApplyVat())
                .devise(facture.getDevise())
                .notes(facture.getNotes())
                .pdfPath(facture.getPdfPath())
                .createdBy(facture.getCreatedBy())
                .idBonReception(facture.getIdBonReception())
                .numeroBonReception(facture.getNumeroBonReception())
                .dateSysteme(facture.getDateSysteme())
                .organizationId(facture.getOrganizationId())
                .agencyId(facture.getAgencyId())
                .build();
    }
}
