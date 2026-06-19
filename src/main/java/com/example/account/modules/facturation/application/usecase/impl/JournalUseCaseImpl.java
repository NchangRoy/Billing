package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.port.input.JournalUseCase;
import com.example.account.modules.facturation.domain.port.output.JournalRepositoryPort;
import com.example.account.modules.facturation.domain.port.output.JournalEventPort;
import com.example.account.modules.facturation.dto.request.JournalCreateRequest;
import com.example.account.modules.facturation.dto.request.JournalUpdateRequest;
import com.example.account.modules.facturation.dto.response.JournalResponse;
import com.example.account.modules.facturation.mapper.JournalMapper;
import com.example.account.modules.facturation.domain.model.Journal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JournalUseCaseImpl implements JournalUseCase {

    private final JournalRepositoryPort journalRepositoryPort;
    private final JournalEventPort journalEventPort;
    private final JournalMapper journalMapper;

    @Override
    @Transactional
    public Mono<JournalResponse> createJournal(JournalCreateRequest request) {
        log.info("Création d'un nouveau journal: {}", request.getNomJournal());

        return journalRepositoryPort.existsByNomJournal(request.getNomJournal())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Un journal avec ce nom existe déjà"));
                    }
                    Journal journal = journalMapper.toEntity(request);
                    if (journal.getIdJournal() == null) {
                        journal.setIdJournal(UUID.randomUUID());
                    }
                    return journalRepositoryPort.save(journal);
                })
                .map(savedJournal -> {
                    JournalResponse response = journalMapper.toResponse(savedJournal);
                    journalEventPort.publishJournalCreated(response);
                    log.info("Journal créé avec succès: {}", savedJournal.getIdJournal());
                    return response;
                });
    }

    @Override
    @Transactional
    public Mono<JournalResponse> updateJournal(UUID journalId, JournalUpdateRequest request) {
        log.info("Mise à jour du journal: {}", journalId);

        return journalRepositoryPort.findById(journalId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Journal non trouvé: " + journalId)))
                .flatMap(journal -> {
                    journalMapper.updateEntityFromRequest(request, journal);
                    return journalRepositoryPort.save(journal);
                })
                .map(updatedJournal -> {
                    JournalResponse response = journalMapper.toResponse(updatedJournal);
                    journalEventPort.publishJournalUpdated(response);
                    log.info("Journal mis à jour avec succès: {}", journalId);
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<JournalResponse> getJournalById(UUID journalId) {
        log.info("Récupération du journal: {}", journalId);
        return journalRepositoryPort.findById(journalId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Journal non trouvé: " + journalId)))
                .map(journalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<JournalResponse> getJournalByNom(String nomJournal) {
        log.info("Récupération du journal par nom: {}", nomJournal);
        return journalRepositoryPort.findByNomJournal(nomJournal)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Journal non trouvé avec nom: " + nomJournal)))
                .map(journalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<JournalResponse> getAllJournals() {
        log.info("Récupération de tous les journals");
        return journalRepositoryPort.findAll()
                .map(journalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<JournalResponse> getAllJournals(Pageable pageable) {
        log.info("Récupération de tous les journals avec pagination");
        return journalRepositoryPort.findAll()
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .map(journalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<JournalResponse> getJournalsByType(String type) {
        log.info("Récupération des journals par type: {}", type);
        return journalRepositoryPort.findByType(type)
                .map(journalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<JournalResponse> searchJournalsByNom(String nomJournal) {
        log.info("Recherche des journals par nom: {}", nomJournal);
        return journalRepositoryPort.findByNomJournalContaining(nomJournal)
                .map(journalMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> deleteJournal(UUID journalId) {
        log.info("Suppression du journal: {}", journalId);
        return journalRepositoryPort.existsById(journalId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Journal non trouvé: " + journalId));
                    }
                    return journalRepositoryPort.deleteById(journalId)
                            .then(Mono.fromRunnable(() -> journalEventPort.publishJournalDeleted(journalId)));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Long> countByType(String type) {
        return journalRepositoryPort.countByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<JournalResponse> getJournalsByOrganizationId(UUID organizationId) {
        log.info("Récupération des journaux par organisation: {}", organizationId);
        return journalRepositoryPort.findByOrganizationId(organizationId).map(journalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<JournalResponse> getJournalsByAgencyId(UUID agencyId) {
        log.info("Récupération des journaux par agence: {}", agencyId);
        return journalRepositoryPort.findByAgencyId(agencyId).map(journalMapper::toResponse);
    }
}
