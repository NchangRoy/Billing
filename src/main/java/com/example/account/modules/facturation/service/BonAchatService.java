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

        return entityTemplate.insert(bonAchat)
                .flatMap(savedBonAchat -> {
                    log.debug("Bon d'achat sauvegardé avec succès: {}", savedBonAchat.getIdBonAchat());
                    return grantOwnerPermission(
                            savedBonAchat.getCreatedBy(),
                            savedBonAchat.getIdBonAchat(),
                            bonAchatMapper.toResponse(savedBonAchat));
                });
    }

    /**
     * PUT - Mettre à jour un bon d'achat existant
     */
    @Transactional
    public Mono<BonAchatResponse> updateBonAchat(UUID id, BonAchatRequest request) {
        log.info("Mise à jour du bon d'achat ID: {}", id);

        return bonAchatRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Bon d'achat non trouvé avec l'ID: " + id)))
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
     * they've never had it, then emails a "new document" notification.
     */
    @Transactional
    public Mono<Void> sendToPortal(UUID id) {
        log.info("Envoi du bon d'achat {} vers le portail fournisseur", id);

        return bonAchatRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon d'achat non trouvé: " + id)))
                .flatMap(bonAchat -> {
                    if (bonAchat.getSupplierEmail() == null || bonAchat.getSupplierEmail().isBlank()) {
                        return Mono.error(new IllegalStateException("Le fournisseur n'a pas d'adresse email renseignée."));
                    }
                    bonAchat.setStatut(StatutBonAchat.ENVOYE);
                    return bonAchatRepository.save(bonAchat);
                })
                .flatMap(bonAchat -> fournisseurUseCase.ensureFournisseurPortalAccess(
                                bonAchat.getIdFournisseur(), bonAchat.getSupplierEmail(), bonAchat.getNomFournisseur())
                        .then(emailService.sendPortalDocumentNotification(
                                bonAchat.getSupplierEmail(), bonAchat.getNomFournisseur(),
                                "Bon de commande", bonAchat.getNumeroBonAchat(),
                                portalFrontendUrl + "/portal/login")));
    }
}