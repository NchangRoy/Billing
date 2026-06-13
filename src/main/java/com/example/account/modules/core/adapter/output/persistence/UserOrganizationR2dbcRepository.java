package com.example.account.modules.core.adapter.output.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserOrganizationR2dbcRepository extends R2dbcRepository<UserOrganizationPersistenceEntity, UUID> {

    Flux<UserOrganizationPersistenceEntity> findByUserId(UUID userId);

    Flux<UserOrganizationPersistenceEntity> findByOrganizationId(UUID organizationId);

    @Query("SELECT * FROM user_organizations WHERE user_id = :userId AND organization_id = :organizationId")
    Mono<UserOrganizationPersistenceEntity> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    @Query("SELECT * FROM user_organizations WHERE user_id = :userId AND is_active = true")
    Flux<UserOrganizationPersistenceEntity> findActiveByUserId(UUID userId);

    @Query("SELECT * FROM user_organizations WHERE user_id = :userId AND is_default = true")
    Mono<UserOrganizationPersistenceEntity> findDefaultByUserId(UUID userId);

    Mono<Boolean> existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
