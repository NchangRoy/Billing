package com.example.account.modules.facturation.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.example.account.modules.facturation.dto.request.BonCommandeCreateRequest;
import com.example.account.modules.facturation.dto.request.BonCommandeUpdateRequest;
import com.example.account.modules.facturation.dto.response.BonCommandeResponse;
import com.example.account.modules.facturation.mapper.BonCommandeMapper;
import com.example.account.modules.facturation.model.entity.BonCommande;
import com.example.account.modules.facturation.domain.model.Devis;
import com.example.account.modules.facturation.model.enums.StatusBonCommande;
import com.example.account.modules.facturation.repository.BonCommandeRepository;
import com.example.account.modules.facturation.service.producer.BonCommandeEventProducer;
import com.example.account.modules.facturation.dto.request.AssignDocPermissionRequest;
import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BonCommandeService {

    private final BonCommandeRepository bonCommandeRepository;
    private final BonCommandeEventProducer bonCommandeEventProducer;
    private final BonCommandeMapper bonCommandeMapper;
    private final R2dbcEntityTemplate entityTemplate;
    private final DocPermissionService docPermissionService;

    private <T> Mono<T> grantOwnerPermission(UUID sellerId, UUID docId, T response) {
        if (sellerId == null || docId == null) return Mono.just(response);
        AssignDocPermissionRequest request = new AssignDocPermissionRequest();
        request.setSellerId(sellerId);
        request.setDocId(docId);
        request.setDocType(DocType.BON_COMMANDE);
        request.setPermission(DocPermissionLevel.OWNER);
        return docPermissionService.grant(request)
                .thenReturn(response)
                .onErrorResume(e -> {
                    log.error("Failed to grant owner doc-permission for bon commande {}: {}", docId, e.getMessage());
                    return Mono.just(response);
                });
    }

    @Transactional
    public Mono<BonCommandeResponse> createBonCommande(BonCommandeCreateRequest request) {
        log.info("Création d'un nouveau bon de commande: {}", request.getNumeroCommande());

        BonCommande bonCommande = bonCommandeMapper.toEntity(request);
        if (bonCommande.getIdBonCommande() == null) {
            bonCommande.setIdBonCommande(UUID.randomUUID());
        }

        return entityTemplate.insert(bonCommande)
                .flatMap(savedBonCommande -> {
                    BonCommandeResponse response = bonCommandeMapper.toResponse(savedBonCommande);
                    bonCommandeEventProducer.publishBonCommandeCreated(response);
                    log.info("Bon de commande créé avec succès: {}", savedBonCommande.getIdBonCommande());
                    return grantOwnerPermission(savedBonCommande.getCreatedBy(), savedBonCommande.getIdBonCommande(), response);
                });
    }

    @Transactional
    public Mono<BonCommandeResponse> updateBonCommande(UUID bonCommandeId, BonCommandeCreateRequest request) {
        log.info("Mise à jour du bon de commande: {}", bonCommandeId);

        return bonCommandeRepository.findById(bonCommandeId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon de commande non trouvé: " + bonCommandeId)))
                .flatMap(bonCommande -> {
                    bonCommandeMapper.updateEntityFromRequest(request, bonCommande);
                    return bonCommandeRepository.save(bonCommande);
                })
                .map(updatedBonCommande -> {
                    BonCommandeResponse response = bonCommandeMapper.toResponse(updatedBonCommande);
                    bonCommandeEventProducer.publishBonCommandeUpdated(response);
                    log.info("Bon de commande mis à jour avec succès: {}", bonCommandeId);
                    return response;
                });
    }

    @Transactional(readOnly = true)
    public Mono<BonCommandeResponse> getBonCommandeById(UUID bonCommandeId) {
        log.info("Récupération du bon de commande: {}", bonCommandeId);

        return bonCommandeRepository.findById(bonCommandeId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon de commande non trouvé: " + bonCommandeId)))
                .map(bonCommandeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Mono<BonCommandeResponse> getBonCommandeByNumero(String numeroCommande) {
        log.info("Récupération du bon de commande par numéro: {}", numeroCommande);

        return bonCommandeRepository.findByNumeroCommande(numeroCommande)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon de commande non trouvé avec numéro: " + numeroCommande)))
                .map(bonCommandeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<BonCommandeResponse> getAllBonCommandes() {
        log.info("Récupération de tous les bons de commande");
        return bonCommandeRepository.findAll()
                .map(bonCommandeMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<BonCommandeResponse> getAllBonCommandes(Pageable pageable) {
        log.info("Récupération de tous les bons de commande avec pagination");
        return bonCommandeRepository.findAll()
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .map(bonCommandeMapper::toResponse);
    }

    @Transactional
    public Mono<BonCommandeResponse> updateStatut(UUID bonCommandeId, StatusBonCommande nouveauStatut) {
        log.info("Mise à jour du statut du bon de commande {} vers {}", bonCommandeId, nouveauStatut);

        return bonCommandeRepository.findById(bonCommandeId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon de commande non trouvé: " + bonCommandeId)))
                .flatMap(bonCommande -> {
                    bonCommande.setStatut(nouveauStatut);
                    bonCommande.setUpdatedAt(LocalDateTime.now());
                    return bonCommandeRepository.save(bonCommande);
                })
                .map(updatedBonCommande -> {
                    BonCommandeResponse response = bonCommandeMapper.toResponse(updatedBonCommande);
                    bonCommandeEventProducer.publishBonCommandeUpdated(response);
                    log.info("Statut du bon de commande mis à jour avec succès: {}", bonCommandeId);
                    return response;
                });
    }

    @Transactional
    public Mono<Void> deleteBonCommande(UUID bonCommandeId) {
        log.info("Suppression du bon de commande: {}", bonCommandeId);
        return bonCommandeRepository.existsById(bonCommandeId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Bon de commande non trouvé: " + bonCommandeId));
                    }
                    return bonCommandeRepository.deleteById(bonCommandeId)
                            .then(Mono.fromRunnable(() -> bonCommandeEventProducer.publishBonCommandeDeleted(bonCommandeId)));
                })
                .then();
    }


   @Transactional(readOnly = true)
   public Flux<BonCommandeResponse> getByOrganizationId(UUID organizationId) {
       return bonCommandeRepository.findByOrganizationId(organizationId).map(bonCommandeMapper::toResponse);
   }

   @Transactional(readOnly = true)
   public Flux<BonCommandeResponse> getByAgencyId(UUID agencyId) {
       return bonCommandeRepository.findByIdAgency(agencyId).map(bonCommandeMapper::toResponse);
   }

   @Transactional(readOnly = true)
   public Flux<BonCommandeResponse> getBySellerId(UUID sellerId) {
       return docPermissionService.findBySellerAndDocType(sellerId, DocType.BON_COMMANDE)
               .flatMap(permission -> bonCommandeRepository.findById(permission.getDocId())
                       .map(entity -> {
                           BonCommandeResponse response = bonCommandeMapper.toResponse(entity);
                           response.setDocPermission(docPermissionService.toResponse(permission));
                           return response;
                       }));
   }

   public Mono<BonCommande> createFromQuotation(Devis devis) {
    log.info("Création d'un nouveau bon de commande depuis le devis: {}", devis.getNumeroDevis());

    // 1. Map the Devis to BonCommande entity
    BonCommande bonCommande = bonCommandeMapper.mapDevisToBonCommande(devis);
    
    // Ensure ID is set if mapper didn't handle it
    if (bonCommande.getIdBonCommande() == null) {
        bonCommande.setIdBonCommande(UUID.randomUUID());
    }

    // 2. Insert into DB
    return entityTemplate.insert(bonCommande)
            .doOnNext(saved -> {
                // 3. Side Effect: Publish event and log
                // We map to Response only for the producer side effect
                BonCommandeResponse response = bonCommandeMapper.toResponse(saved);
                bonCommandeEventProducer.publishBonCommandeCreated(response);
                log.info("Bon de commande créé avec succès: {}", saved.getIdBonCommande());
            });
            // Returns the saved BonCommande entity as requested in the method signature
}
}

 