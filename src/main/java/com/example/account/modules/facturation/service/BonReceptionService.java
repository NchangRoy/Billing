package com.example.account.modules.facturation.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.example.account.modules.facturation.dto.request.BondeReceptionCreateRequest;
import com.example.account.modules.facturation.dto.response.BondeReceptionResponse;
import com.example.account.modules.facturation.mapper.BondeReceptionMapper;
import com.example.account.modules.facturation.model.entity.BondeReception;
import com.example.account.modules.facturation.repository.BonReceptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BonReceptionService {

    private final BonReceptionRepository bonReceptionRepository;
    private final BondeReceptionMapper bondeReceptionMapper;
    private final R2dbcEntityTemplate entityTemplate;

    @Transactional
    public Mono<BondeReceptionResponse> createBondeReception(BondeReceptionCreateRequest dto) {
        log.info("Création d'un nouveau Bon de Réception");
        BondeReception bondeReception = bondeReceptionMapper.toEntity(dto);
        if (bondeReception.getIdBonReception() == null) {
            bondeReception.setIdBonReception(UUID.randomUUID());
        }
        return entityTemplate.insert(bondeReception)
                .map(bondeReceptionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Flux<BondeReceptionResponse> getAllBondeReception() {
        log.info("Récupération de tous les Bons de Réception");
        return bonReceptionRepository.findAll()
                .map(bondeReceptionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Mono<BondeReceptionResponse> getBondeReceptionById(UUID id) {
        log.info("Récupération du Bon de Réception ID: {}", id);
        // Search with reactive context support via ReactiveOrganizationContext
        return bonReceptionRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon de Réception non trouvé")))
                .map(bondeReceptionMapper::toDto);
    }

    @Transactional
    public Mono<BondeReceptionResponse> updateBondeReception(UUID id, BondeReceptionResponse dto) {
        log.info("Mise à jour du Bon de Réception: {}", id);
        return bonReceptionRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon de Réception non trouvé")))
                .flatMap(bondeReception -> {
                    UUID originalId = bondeReception.getIdBonReception();
                    UUID originalOrgId = bondeReception.getOrganizationId();
                    bondeReceptionMapper.updateEntityFromDto(dto, bondeReception);
                    bondeReception.setIdBonReception(originalId);
                    if (bondeReception.getOrganizationId() == null) {
                        bondeReception.setOrganizationId(originalOrgId);
                    }
                    return bonReceptionRepository.save(bondeReception);
                })
                .map(bondeReceptionMapper::toDto);
    }

    @Transactional
    public Mono<Void> deleteBondeReception(UUID id) {
        log.info("Suppression du Bon de Réception: {}", id);
        return bonReceptionRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Bon de Réception non trouvé")))
                .flatMap(bondeReception -> bonReceptionRepository.delete(bondeReception));
    }

    @Transactional(readOnly = true)
    public Flux<BondeReceptionResponse> getByOrganizationId(UUID organizationId) {
        return bonReceptionRepository.findByOrganizationId(organizationId).map(bondeReceptionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Flux<BondeReceptionResponse> getByAgencyId(UUID agencyId) {
        return bonReceptionRepository.findByAgencyId(agencyId).map(bondeReceptionMapper::toDto);
    }
}
