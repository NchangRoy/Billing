package com.example.account.modules.facturation.adapter.output.persistence;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface FactureActorIndexRepository extends R2dbcRepository<FactureActorIndexEntity, UUID> {
    Flux<FactureActorIndexEntity> findByActorId(UUID actorId);
}
