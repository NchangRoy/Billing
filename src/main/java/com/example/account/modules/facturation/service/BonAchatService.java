package com.example.account.modules.facturation.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.example.account.modules.facturation.dto.request.BonAchatRequest;
import com.example.account.modules.facturation.dto.response.BonAchatResponse;
import com.example.account.modules.facturation.mapper.BonAchatMapper;
import com.example.account.modules.facturation.model.entity.BonAchat;
import com.example.account.modules.facturation.repository.BonAchatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                .map(savedBonAchat -> {
                    log.debug("Bon d'achat sauvegardé avec succès: {}", savedBonAchat.getIdBonAchat());
                    return bonAchatMapper.toResponse(savedBonAchat);
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
}