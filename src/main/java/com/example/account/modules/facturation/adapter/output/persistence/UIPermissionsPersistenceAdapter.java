package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.adapter.output.persistence.mapper.UIPermissionsPersistenceMapper;
import com.example.account.modules.facturation.domain.model.UIPermissions;
import com.example.account.modules.facturation.domain.port.output.UIPermissionsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class UIPermissionsPersistenceAdapter implements UIPermissionsRepositoryPort {

    private final UIPermissionsR2dbcRepository repository;
    private final UIPermissionsPersistenceMapper mapper;
    private final R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<UIPermissions> insert(UIPermissions uiPermissions) {
        return entityTemplate.insert(mapper.toEntity(uiPermissions)).map(mapper::toDomain);
    }

    @Override
    public Mono<UIPermissions> save(UIPermissions uiPermissions) {
        return repository.save(mapper.toEntity(uiPermissions)).map(mapper::toDomain);
    }

    @Override
    public Mono<UIPermissions> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<UIPermissions> findBySellerId(UUID sellerId) {
        return repository.findBySellerId(sellerId).map(mapper::toDomain);
    }

    @Override
    public Flux<UIPermissions> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }

    @Override
    public Flux<UIPermissions> findByAgencyId(UUID agencyId) {
        return repository.findByAgencyId(agencyId).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }
}
