package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.model.NoteCredit;
import com.example.account.modules.facturation.domain.port.input.NoteCreditUseCase;
import com.example.account.modules.facturation.domain.port.output.NoteCreditRepositoryPort;
import com.example.account.modules.facturation.dto.request.NoteCreditRequest;
import com.example.account.modules.facturation.dto.response.NoteCreditResponse;
import com.example.account.modules.facturation.mapper.NoteCreditMapper;
import com.example.account.modules.facturation.dto.request.AssignDocPermissionRequest;
import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;
import com.example.account.modules.facturation.service.DocPermissionService;
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
    private final DocPermissionService docPermissionService;

    private <T> Mono<T> grantOwnerPermission(UUID sellerId, UUID docId, T response) {
        if (sellerId == null || docId == null) return Mono.just(response);
        AssignDocPermissionRequest request = new AssignDocPermissionRequest();
        request.setSellerId(sellerId);
        request.setDocId(docId);
        request.setDocType(DocType.NOTE_CREDIT);
        request.setPermission(DocPermissionLevel.OWNER);
        return docPermissionService.grant(request)
                .thenReturn(response)
                .onErrorResume(e -> {
                    log.error("Failed to grant owner doc-permission for note credit {}: {}", docId, e.getMessage());
                    return Mono.just(response);
                });
    }

    @Override
    @Transactional
    public Mono<NoteCreditResponse> createNoteCredit(NoteCreditRequest request) {
        log.info("Création d'une nouvelle note de crédit");
        NoteCredit entity = noteCreditMapper.toEntity(request);
        if (entity.getIdNoteCredit() == null) {
            entity.setIdNoteCredit(UUID.randomUUID());
        }
        return noteCreditRepository.insert(entity)
                .flatMap(savedEntity -> grantOwnerPermission(
                        savedEntity.getCreatedBy(),
                        savedEntity.getIdNoteCredit(),
                        noteCreditMapper.toResponse(savedEntity)));
    }

    @Override
    @Transactional
    public Mono<NoteCreditResponse> updateNoteCredit(UUID id, NoteCreditRequest request) {
        log.info("Mise à jour de la note de crédit: {}", id);
        return noteCreditRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Note de crédit non trouvée")))
                .flatMap(entity -> {
                    UUID originalOrgId = entity.getOrganizationId();
                    UUID originalId = entity.getIdNoteCredit();
                    noteCreditMapper.updateEntityFromRequest(request, entity);
                    entity.setIdNoteCredit(originalId);
                    if (entity.getOrganizationId() == null) {
                        entity.setOrganizationId(originalOrgId);
                    }
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

    @Override
    @Transactional(readOnly = true)
    public Flux<NoteCreditResponse> getNotesCreditByOrganizationId(UUID organizationId) {
        log.info("Récupération des notes de crédit par organisation: {}", organizationId);
        return noteCreditRepository.findByOrganizationId(organizationId).map(noteCreditMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<NoteCreditResponse> getNotesCreditByAgencyId(UUID agencyId) {
        log.info("Récupération des notes de crédit par agence: {}", agencyId);
        return noteCreditRepository.findByAgencyId(agencyId).map(noteCreditMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<NoteCreditResponse> getNotesCreditBySellerId(UUID sellerId) {
        log.info("Récupération des notes de crédit accessibles par le vendeur: {}", sellerId);
        return docPermissionService.findBySellerAndDocType(sellerId, DocType.NOTE_CREDIT)
                .flatMap(permission -> noteCreditRepository.findById(permission.getDocId())
                        .map(entity -> {
                            NoteCreditResponse response = noteCreditMapper.toResponse(entity);
                            response.setDocPermission(docPermissionService.toResponse(permission));
                            return response;
                        }));
    }
}
