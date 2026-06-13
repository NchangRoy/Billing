package com.example.account.modules.core.domain.port.output;

import com.example.account.modules.core.domain.model.UserOrganization;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserOrganizationRepositoryPort {
    Flux<UserOrganization> findByUserId(UUID userId);
    Flux<UserOrganization> findByOrganizationId(UUID organizationId);
    Mono<UserOrganization> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
    Flux<UserOrganization> findActiveByUserId(UUID userId);
    Mono<UserOrganization> findDefaultByUserId(UUID userId);
    Mono<Boolean> existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);
    Mono<UserOrganization> save(UserOrganization userOrganization);
}
