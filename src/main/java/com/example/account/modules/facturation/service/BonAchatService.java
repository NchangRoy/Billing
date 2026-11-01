package com.example.account.modules.facturation.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.example.account.modules.facturation.dto.request.BonAchatRequest;
import com.example.account.modules.facturation.dto.response.BonAchatResponse;
import com.example.account.modules.facturation.mapper.BonAchatMapper;
import com.example.account.modules.facturation.model.entity.BonAchat;
import com.example.account.modules.facturation.model.enums.StatutBonAchat;
import com.example.account.modules.facturation.repository.BonAchatRepository;
import com.example.account.modules.tiers.domain.port.input.FournisseurUseCase;
import com.example.account.modules.tiers.domain.port.input.ThirdPartyContactUseCase;
import com.example.account.modules.facturation.dto.request.AssignDocPermissionRequest;
import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BonAchatService {

    private final BonAchatRepository bonAchatRepository;
    private final BonAchatMapper bonAchatMapper;
    private final R2dbcEntityTemplate entityTemplate;
    private final FournisseurUseCase fournisseurUseCase;
    private final ThirdPartyContactUseCase thirdPartyContactUseCase;
    private final EmailService emailService;
    private final DocPermissionService docPermissionService;

    @Value("${client-portal.frontend-url}")
    private String portalFrontendUrl;

    private <T> Mono<T> grantOwnerPermission(UUID sellerId, UUID docId, T response) {
        if (sellerId == null || docId == null) return Mono.just(response);
        AssignDocPermissionRequest request = new AssignDocPermissionRequest();
        request.setSellerId(sellerId);
        request.setDocId(docId);
        request.setDocType(DocType.BON_ACHAT);
        request.setPermission(DocPermissionLevel.OWNER);
        return docPermissionService.grant(request)
                .thenReturn(response)
                .onErrorResume(e -> {
                    log.error("Failed to grant owner doc-permission for bon achat {}: {}", docId, e.getMessage());
                    return Mono.just(response);
                });
    }

    /**
     * POST - Créer un nouveau bon d'achat
     */
    @Transactional
    public Mono<BonAchatResponse> createBonAchat(BonAchatRequest request) {
        log.info("Création d'un nouveau bon d'achat, numéro: {}", request.getNumeroBonAchat());
        System.out.println(request);
        BonAchat bonAchat = bonAchatMapper.toEntity(request);
        bonAchat.setOrganizationId(request.getOrganizationId());
        bonAchat.setAgencyId(request.getAgencyId());
        System.out.println(bonAchat);
        if (bonAchat.getIdBonAchat() == null) {
            bonAchat.setIdBonAchat(UUID.randomUUID());
        }

        return withActorId(bonAchat)
                .flatMap(entityTemplate::insert)
                .flatMap(savedBonAchat -> {
                    log.debug("Bon d'achat sauvegardé avec succès: {}", savedBonAchat.getIdBonAchat());
                    return grantOwnerPermission(
                            savedBonAchat.getCreatedBy(),
                            savedBonAchat.getIdBonAchat(),
                            bonAchatMapper.toResponse(savedBonAchat));
                });
    }

    /**
     * Stamps the fournisseur's Kernel actor id onto the bon d'achat at
     * creation time — see DevisUseCaseImpl.withActorId for the full
     * reasoning (client portal "my purchase orders" list).
     */
    private Mono<BonAchat> withActorId(BonAchat bonAchat) {
        if (bonAchat.getIdFournisseur() == null) {
            return Mono.just(bonAchat);
        }
        return thirdPartyContactUseCase.resolveActorId(bonAchat.getIdFournisseur())
                .doOnNext(bonAchat::setActorId)
                .onErrorResume(e -> {
                    log.warn("Could not resolve actor id for fournisseur {} (continuing without it): {}", bonAchat.getIdFournisseur(), e.getMessage());
                    return Mono.empty();
                })
                .thenReturn(bonAchat);
    }

    /**
     * PUT - Mettre à jour un bon d'achat existant
     */
    @Transactional
    public Mono<BonAchatResponse> updateBonAchat(UUID id, BonAchatRequest request) {
        log.info("Mise à jour du bon d'achat ID: {}", id);

        return bonAchatRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon d'achat non trouvé avec l'ID: " + id)))
                .flatMap(existingBonAchat -> {
                    bonAchatMapper.updateEntityFromRequest(request, existingBonAchat);
                    return bonAchatRepository.save(existingBonAchat);
                })
                .map(bonAchatMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Mono<BonAchatResponse> getBonAchatById(UUID id) {
        log.info("Récupération du bon d'achat ID: {}", id);
        return bonAchatRepository.findById(id)
                .map(bonAchatMapper::toResponse)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon d'achat non trouvé: " + id)));
    }

    @Transactional(readOnly = true)
    public Flux<BonAchatResponse> getAllBonsAchat() {
        log.info("Récupération de tous les bons d'achat");
        return bonAchatRepository.findAll()
                .map(bonAchatMapper::toResponse);
    }

    @Transactional
    public Mono<Void> deleteBonAchat(UUID id) {
        log.info("Suppression du bon d'achat ID: {}", id);
        return bonAchatRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Bon d'achat non trouvé: " + id));
                    }
                    return bonAchatRepository.deleteById(id);
                })
                .doOnSuccess(v -> log.info("Bon d'achat ID: {} supprimé", id));
    }

    @Transactional(readOnly = true)
    public Flux<BonAchatResponse> getByOrganizationId(UUID organizationId) {
        return bonAchatRepository.findByOrganizationId(organizationId).map(bonAchatMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<BonAchatResponse> getByAgencyId(UUID agencyId) {
        return bonAchatRepository.findByAgencyId(agencyId).map(bonAchatMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<BonAchatResponse> getBySupplierId(UUID supplierId) {
        return bonAchatRepository.findByIdFournisseur(supplierId).map(bonAchatMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<BonAchatResponse> getBySellerId(UUID sellerId) {
        return docPermissionService.findBySellerAndDocType(sellerId, DocType.BON_ACHAT)
                .flatMap(permission -> bonAchatRepository.findById(permission.getDocId())
                        .map(entity -> {
                            BonAchatResponse response = bonAchatMapper.toResponse(entity);
                            response.setDocPermission(docPermissionService.toResponse(permission));
                            return response;
                        }));
    }

    @Transactional
    public Mono<Void> accepterBonAchat(UUID id) {
        log.info("Acceptation du bon d'achat: {}", id);
        return bonAchatRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon d'achat non trouvé: " + id)))
                .flatMap(bonAchat -> {
                    bonAchat.setStatut(StatutBonAchat.ACCEPTE);
                    return bonAchatRepository.save(bonAchat);
                })
                .then();
    }

    @Transactional
    public Mono<Void> refuserBonAchat(UUID id) {
        log.info("Refus du bon d'achat: {}", id);
        return bonAchatRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon d'achat non trouvé: " + id)))
                .flatMap(bonAchat -> {
                    bonAchat.setStatut(StatutBonAchat.REJETE);
                    return bonAchatRepository.save(bonAchat);
                })
                .then();
    }

    /**
     * Same pattern as DevisUseCaseImpl.sendToPortal — marks the purchase order
     * ENVOYE, bootstraps the supplier's (login-based) portal access only if
     * they've never had it, then emails a "new document" notification. Only
     * fires from BROUILLON: calling this again on an already-sent purchase
     * order is a no-op, so double-submits don't re-provision access or
     * re-send the email. Status is only persisted once the email has
     * actually gone out, so a failed send never leaves the order falsely
     * marked as sent (this used to save ENVOYE *before* attempting the
     * email/portal-access call — fixed here to match DevisUseCaseImpl's
     * correct ordering).
     */
    @Transactional
    public Mono<Void> sendToPortal(UUID id, com.example.account.modules.facturation.dto.request.ShareDocumentRequest request) {
        log.info("Envoi du bon d'achat {} vers le portail fournisseur", id);

        return bonAchatRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon d'achat non trouvé: " + id)))
                .flatMap(bonAchat -> {
                    if (bonAchat.getStatut() != StatutBonAchat.BROUILLON) {
                        log.info("Bon d'achat {} déjà envoyé (statut: {}), envoi ignoré", id, bonAchat.getStatut());
                        return Mono.empty();
                    }
                    // Lets the "Send to Contact" picker target any address-book
                    // contact instead of the bon d'achat's own stored
                    // supplierEmail — falls back to that stored email when no
                    // contact was picked.
                    String targetEmail = (request != null && request.getContactEmail() != null && !request.getContactEmail().isBlank())
                            ? request.getContactEmail() : bonAchat.getSupplierEmail();
                    String targetName = (request != null && request.getContactName() != null && !request.getContactName().isBlank())
                            ? request.getContactName() : bonAchat.getNomFournisseur();
                    if (targetEmail == null || targetEmail.isBlank()) {
                        return Mono.error(new IllegalStateException("Le fournisseur n'a pas d'adresse email renseignée."));
                    }
                    return fournisseurUseCase.ensureFournisseurPortalAccess(
                                    bonAchat.getIdFournisseur(), targetEmail, targetName)
                            // Kernel's ensure-portal-access endpoint isn't deployed yet (404) — this
                            // is a bootstrap-only nicety anyway (grants login to brand-new fournisseurs),
                            // so a failure here shouldn't block the actual notification email.
                            .onErrorResume(e -> {
                                log.warn("ensureFournisseurPortalAccess failed for bon d'achat {} (continuing anyway): {}", id, e.getMessage());
                                return Mono.empty();
                            })
                            .then(emailService.sendPortalDocumentNotification(
                                    targetEmail, targetName,
                                    "Bon de commande", bonAchat.getNumeroBonAchat(),
                                    portalFrontendUrl + "/portal/login"))
                            .then(Mono.defer(() -> {
                                bonAchat.setStatut(StatutBonAchat.ENVOYE);
                                return bonAchatRepository.save(bonAchat);
                            }));
                })
                .then();
    }
}