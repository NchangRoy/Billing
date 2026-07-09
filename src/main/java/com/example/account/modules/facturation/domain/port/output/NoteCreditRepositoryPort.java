package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.domain.model.NoteCredit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface NoteCreditRepositoryPort {
    Mono<NoteCredit> findById(UUID id);
    Mono<NoteCredit> findByNumeroNoteCredit(String numeroNoteCredit);
    Flux<NoteCredit> findByIdClient(UUID idClient);
    Flux<NoteCredit> findByIdFactureOrigine(UUID idFactureOrigine);
    Mono<Boolean> existsByNumeroNoteCredit(String numeroNoteCredit);
    Mono<Boolean> existsById(UUID id);
    Flux<NoteCredit> findByOrganizationId(UUID organizationId);
    Flux<NoteCredit> findByAgencyId(UUID agencyId);
    Flux<NoteCredit> findByCreatedBy(UUID createdBy);
    Mono<Void> deleteById(UUID id);
    Mono<NoteCredit> save(NoteCredit noteCredit);
    Mono<NoteCredit> insert(NoteCredit noteCredit);
    Flux<NoteCredit> findAll();
    Mono<Void> delete(NoteCredit noteCredit);
}
