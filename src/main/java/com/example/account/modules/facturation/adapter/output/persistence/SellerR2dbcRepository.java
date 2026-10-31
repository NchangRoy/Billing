package com.example.account.modules.facturation.adapter.output.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

public interface SellerR2dbcRepository extends ReactiveCrudRepository<SellerPersistenceEntity, UUID> {
    Flux<SellerPersistenceEntity> findByOrganizationId(UUID organizationId);
    Flux<SellerPersistenceEntity> findByOrganizationIdAndIdIn(UUID organizationId, Collection<UUID> ids);
    Mono<SellerPersistenceEntity> findByOrganizationIdAndEmailIgnoreCase(UUID organizationId, String email);
}
