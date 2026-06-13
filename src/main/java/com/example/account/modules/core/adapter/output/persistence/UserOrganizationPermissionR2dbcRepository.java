package com.example.account.modules.core.adapter.output.persistence;

import com.example.account.modules.core.model.enums.Permission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserOrganizationPermissionR2dbcRepository extends R2dbcRepository<UserOrganizationPermissionPersistenceEntity, UUID> {

    Flux<UserOrganizationPermissionPersistenceEntity> findByUserOrganizationId(UUID userOrganizationId);

    @Query("SELECT * FROM user_organization_permissions WHERE user_organization_id = :userOrganizationId AND is_active = true")
    Flux<UserOrganizationPermissionPersistenceEntity> findActiveByUserOrganizationId(UUID userOrganizationId);

    @Query("SELECT * FROM user_organization_permissions WHERE user_organization_id = :userOrganizationId AND permission = :permission")
    Mono<UserOrganizationPermissionPersistenceEntity> findByUserOrganizationIdAndPermission(UUID userOrganizationId, Permission permission);

    Mono<Boolean> existsByUserOrganizationIdAndPermission(UUID userOrganizationId, Permission permission);
}
