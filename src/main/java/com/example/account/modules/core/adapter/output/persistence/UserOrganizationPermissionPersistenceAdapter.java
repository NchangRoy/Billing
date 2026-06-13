package com.example.account.modules.core.adapter.output.persistence;

import com.example.account.modules.core.domain.model.UserOrganizationPermission;
import com.example.account.modules.core.domain.port.output.UserOrganizationPermissionRepositoryPort;
import com.example.account.modules.core.model.enums.Permission;
import com.example.account.modules.core.adapter.output.persistence.mapper.UserOrganizationPermissionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserOrganizationPermissionPersistenceAdapter implements UserOrganizationPermissionRepositoryPort {

    private final UserOrganizationPermissionR2dbcRepository repository;
    private final UserOrganizationPermissionPersistenceMapper mapper;

    @Override
    public Flux<UserOrganizationPermission> findByUserOrganizationId(UUID userOrganizationId) {
        return repository.findByUserOrganizationId(userOrganizationId)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<UserOrganizationPermission> findActiveByUserOrganizationId(UUID userOrganizationId) {
        return repository.findActiveByUserOrganizationId(userOrganizationId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<UserOrganizationPermission> findByUserOrganizationIdAndPermission(UUID userOrganizationId, Permission permission) {
        return repository.findByUserOrganizationIdAndPermission(userOrganizationId, permission)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByUserOrganizationIdAndPermission(UUID userOrganizationId, Permission permission) {
        return repository.existsByUserOrganizationIdAndPermission(userOrganizationId, permission);
    }

    @Override
    public Mono<UserOrganizationPermission> save(UserOrganizationPermission permission) {
        return repository.save(mapper.toEntity(permission))
                .map(mapper::toDomain);
    }
}
