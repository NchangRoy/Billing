package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.domain.model.Journal;
import com.example.account.modules.facturation.domain.port.output.JournalRepositoryPort;
import com.example.account.modules.facturation.adapter.output.persistence.mapper.JournalPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JournalPersistenceAdapter implements JournalRepositoryPort {

    private final JournalR2dbcRepository repository;
    private final JournalPersistenceMapper mapper;
    private final org.springframework.data.r2dbc.core.R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<Journal> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<Journal> findByNomJournal(String nomJournal) {
        return repository.findByNomJournal(nomJournal).map(mapper::toDomain);
    }

    @Override
    public Flux<Journal> findByType(String type) {
        return repository.findByType(type).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNomJournal(String nomJournal) {
        return repository.existsByNomJournal(nomJournal);
    }

    @Override
    public Flux<Journal> findByNomJournalContaining(String query) {
        return repository.findByNomJournalContaining(query).map(mapper::toDomain);
    }

    @Override
    public Flux<Journal> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }

    @Override
    public Flux<Journal> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }

    @Override
    public Flux<Journal> findByAgencyId(UUID agencyId) {
        return repository.findByAgencyId(agencyId).map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByType(String type) {
        return repository.countByType(type);
    }

    @Override
    public Mono<Journal> save(Journal journal) {
        JournalPersistenceEntity entity = mapper.toEntity(journal);
        if (entity.getIdJournal() == null) {
            entity.setIdJournal(UUID.randomUUID());
            return entityTemplate.insert(entity).map(mapper::toDomain);
        }
        return repository.existsById(entity.getIdJournal())
                .flatMap(exists -> exists ? repository.save(entity) : entityTemplate.insert(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }
}
