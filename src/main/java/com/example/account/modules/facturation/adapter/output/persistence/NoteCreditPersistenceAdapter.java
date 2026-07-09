package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.adapter.output.persistence.mapper.NoteCreditPersistenceMapper;
import com.example.account.modules.facturation.domain.model.NoteCredit;
import com.example.account.modules.facturation.domain.port.output.NoteCreditRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class NoteCreditPersistenceAdapter implements NoteCreditRepositoryPort {

    private final NoteCreditR2dbcRepository repository;
    private final NoteCreditPersistenceMapper mapper;
    private final R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<NoteCredit> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<NoteCredit> findByNumeroNoteCredit(String numeroNoteCredit) {
        return repository.findByNumeroNoteCredit(numeroNoteCredit).map(mapper::toDomain);
    }

    @Override
    public Flux<NoteCredit> findByIdClient(UUID idClient) {
        return repository.findByIdClient(idClient).map(mapper::toDomain);
    }

    @Override
    public Flux<NoteCredit> findByIdFactureOrigine(UUID idFactureOrigine) {
        return repository.findByIdFactureOrigine(idFactureOrigine).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNumeroNoteCredit(String numeroNoteCredit) {
        return repository.existsByNumeroNoteCredit(numeroNoteCredit);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Flux<NoteCredit> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }

    @Override
    public Flux<NoteCredit> findByAgencyId(UUID agencyId) {
        return repository.findByAgencyId(agencyId).map(mapper::toDomain);
    }

    @Override
    public Flux<NoteCredit> findByCreatedBy(UUID createdBy) {
        return repository.findByCreatedBy(createdBy).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<NoteCredit> save(NoteCredit noteCredit) {
        return repository.save(mapper.toEntity(noteCredit)).map(mapper::toDomain);
    }

    @Override
    public Mono<NoteCredit> insert(NoteCredit noteCredit) {
        return entityTemplate.insert(mapper.toEntity(noteCredit)).map(mapper::toDomain);
    }

    @Override
    public Flux<NoteCredit> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }

    @Override
    public Mono<Void> delete(NoteCredit noteCredit) {
        return repository.delete(mapper.toEntity(noteCredit));
    }
}
