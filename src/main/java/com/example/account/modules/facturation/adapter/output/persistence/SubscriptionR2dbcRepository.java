package com.example.account.modules.facturation.adapter.output.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SubscriptionR2dbcRepository extends ReactiveCrudRepository<SubscriptionPersistenceEntity, UUID> {
    Mono<SubscriptionPersistenceEntity> findByOrganizationId(UUID organizationId);
}
