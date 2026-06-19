package com.example.account.modules.facturation.adapter.output.persistence;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface NoteCreditR2dbcRepository extends R2dbcRepository<NoteCreditPersistenceEntity, UUID> {
    Mono<NoteCreditPersistenceEntity> findByNumeroNoteCredit(String numeroNoteCredit);
    Flux<NoteCreditPersistenceEntity> findByIdClient(UUID idClient);
    Flux<NoteCreditPersistenceEntity> findByIdFactureOrigine(UUID idFactureOrigine);
    Mono<Boolean> existsByNumeroNoteCredit(String numeroNoteCredit);

    Flux<NoteCreditPersistenceEntity> findByOrganizationId(UUID organizationId);

    Flux<NoteCreditPersistenceEntity> findByAgencyId(UUID agencyId);
}
