package com.example.account.modules.facturation.adapter.output.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface JournalR2dbcRepository extends R2dbcRepository<JournalPersistenceEntity, UUID> {
    Mono<JournalPersistenceEntity> findByNomJournal(String nomJournal);
    Flux<JournalPersistenceEntity> findByType(String type);
    Mono<Boolean> existsByNomJournal(String nomJournal);
    Flux<JournalPersistenceEntity> findByNomJournalContaining(String query);
    
    @Query("SELECT COUNT(*) FROM journals WHERE type = :type")
    Mono<Long> countByType(String type);

    Flux<JournalPersistenceEntity> findByOrganizationId(UUID organizationId);

    Flux<JournalPersistenceEntity> findByAgencyId(UUID agencyId);
}
