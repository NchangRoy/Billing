package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.model.UIPermissions;
import com.example.account.modules.facturation.domain.port.input.UIPermissionsUseCase;
import com.example.account.modules.facturation.domain.port.output.UIPermissionsRepositoryPort;
import com.example.account.modules.facturation.dto.request.UIPermissionsRequest;
import com.example.account.modules.facturation.dto.response.UIPermissionsResponse;
import com.example.account.modules.facturation.mapper.UIPermissionsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UIPermissionsUseCaseImpl implements UIPermissionsUseCase {

    private final UIPermissionsRepositoryPort repository;
    private final UIPermissionsMapper mapper;

    @Override
    @Transactional
    public Mono<UIPermissionsResponse> create(UIPermissionsRequest request) {
        log.info("Création des permissions UI pour seller: {}", request.getSellerId());
        UIPermissions entity = mapper.toEntity(request);
        entity.setId(UUID.randomUUID());
        return repository.insert(entity).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<UIPermissionsResponse> update(UUID id, UIPermissionsRequest request) {
        log.info("Mise à jour des permissions UI: {}", id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("UI Permissions not found: " + id)))
                .flatMap(entity -> {
                    mapper.updateEntityFromRequest(request, entity);
                    entity.setId(id);
                    return repository.save(entity);
                })
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<UIPermissionsResponse> getById(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("UI Permissions not found: " + id)))
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<UIPermissionsResponse> getBySellerId(UUID sellerId) {
        log.info("Récupération des permissions UI pour seller: {}", sellerId);
        return repository.findBySellerId(sellerId).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<UIPermissionsResponse> getByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<UIPermissionsResponse> getByAgencyId(UUID agencyId) {
        return repository.findByAgencyId(agencyId).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> delete(UUID id) {
        log.info("Suppression des permissions UI: {}", id);
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("UI Permissions not found: " + id)))
                .flatMap(entity -> repository.deleteById(entity.getId()));
    }
}
