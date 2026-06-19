package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.domain.model.Journal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface JournalRepositoryPort {
    Mono<Journal> findById(UUID id);
    Mono<Journal> findByNomJournal(String nomJournal);
    Flux<Journal> findByType(String type);
    Mono<Boolean> existsByNomJournal(String nomJournal);
    Flux<Journal> findByNomJournalContaining(String query);
    Flux<Journal> findAll();
    Flux<Journal> findByOrganizationId(UUID organizationId);
    Flux<Journal> findByAgencyId(UUID agencyId);
    Mono<Long> countByType(String type);
    Mono<Journal> save(Journal journal);
    Mono<Void> deleteById(UUID id);
    Mono<Boolean> existsById(UUID id);
}
