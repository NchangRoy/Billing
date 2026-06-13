package com.example.account.modules.core.domain.port.output;

import com.example.account.modules.core.domain.model.UserOrganizationPermission;
import com.example.account.modules.core.model.enums.Permission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserOrganizationPermissionRepositoryPort {
    Flux<UserOrganizationPermission> findByUserOrganizationId(UUID userOrganizationId);
    Flux<UserOrganizationPermission> findActiveByUserOrganizationId(UUID userOrganizationId);
    Mono<UserOrganizationPermission> findByUserOrganizationIdAndPermission(UUID userOrganizationId, Permission permission);
    Mono<Boolean> existsByUserOrganizationIdAndPermission(UUID userOrganizationId, Permission permission);
    Mono<UserOrganizationPermission> save(UserOrganizationPermission permission);
}
