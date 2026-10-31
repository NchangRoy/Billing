package com.example.account.modules.facturation.adapter.output.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UIPermissionsR2dbcRepository extends ReactiveCrudRepository<UIPermissionsPersistenceEntity, UUID> {
    Flux<UIPermissionsPersistenceEntity> findBySellerId(UUID sellerId);
    Flux<UIPermissionsPersistenceEntity> findByOrganizationId(UUID organizationId);
    Flux<UIPermissionsPersistenceEntity> findByAgencyId(UUID agencyId);

    // One row per seller is enforced by a unique constraint (see changelog-v1.4) —
    // this is the real lookup for the local-only seller permissions store.
    Mono<UIPermissionsPersistenceEntity> findFirstBySellerId(UUID sellerId);
}
