package com.example.account.modules.facturation.adapter.output.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface UIPermissionsR2dbcRepository extends ReactiveCrudRepository<UIPermissionsPersistenceEntity, UUID> {
    Flux<UIPermissionsPersistenceEntity> findBySellerId(UUID sellerId);
    Flux<UIPermissionsPersistenceEntity> findByOrganizationId(UUID organizationId);
    Flux<UIPermissionsPersistenceEntity> findByAgencyId(UUID agencyId);
}
