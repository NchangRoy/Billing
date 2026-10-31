package com.example.account.modules.facturation.adapter.output.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface SessionR2dbcRepository extends ReactiveCrudRepository<SessionPersistenceEntity, UUID> {
}
