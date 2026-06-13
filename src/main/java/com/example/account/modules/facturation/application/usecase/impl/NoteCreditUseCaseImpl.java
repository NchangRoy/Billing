package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.model.NoteCredit;
import com.example.account.modules.facturation.domain.port.input.NoteCreditUseCase;
import com.example.account.modules.facturation.domain.port.output.NoteCreditRepositoryPort;
import com.example.account.modules.facturation.dto.request.NoteCreditRequest;
import com.example.account.modules.facturation.dto.response.NoteCreditResponse;
import com.example.account.modules.facturation.mapper.NoteCreditMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteCreditUseCaseImpl implements NoteCreditUseCase {

    private final NoteCreditRepositoryPort noteCreditRepository;
    private final NoteCreditMapper noteCreditMapper;

    @Override
    @Transactional
    public Mono<NoteCreditResponse> createNoteCredit(NoteCreditRequest request) {
        log.info("Création d'une nouvelle note de crédit");
        NoteCredit entity = noteCreditMapper.toEntity(request);
        if (entity.getIdNoteCredit() == null) {
            entity.setIdNoteCredit(UUID.randomUUID());
        }
        return noteCreditRepository.insert(entity)
                .map(noteCreditMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<NoteCreditResponse> updateNoteCredit(UUID id, NoteCreditRequest request) {
        log.info("Mise à jour de la note de crédit: {}", id);
        return noteCreditRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Note de crédit non trouvée")))
                .flatMap(entity -> {
                    noteCreditMapper.updateEntityFromRequest(request, entity);
                    return noteCreditRepository.save(entity);
                })
                .map(noteCreditMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<NoteCreditResponse> getNoteCreditById(UUID id) {
        log.info("Récupération de la note de crédit ID: {}", id);
        return noteCreditRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Note de crédit non trouvée")))
                .map(noteCreditMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<NoteCreditResponse> getAllNoteCredits() {
        log.info("Récupération de toutes les notes de crédit");
        return noteCreditRepository.findAll()
                .map(noteCreditMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> deleteNoteCredit(UUID id) {
        log.info("Suppression de la note de crédit: {}", id);
        return noteCreditRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Note de crédit non trouvée")))
                .flatMap(noteCreditRepository::delete);
    }
}
