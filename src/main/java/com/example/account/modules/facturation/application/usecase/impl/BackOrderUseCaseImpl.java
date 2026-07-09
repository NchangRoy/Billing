package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.model.BackOrder;
import com.example.account.modules.facturation.domain.port.input.BackOrderUseCase;
import com.example.account.modules.facturation.domain.port.output.BackOrderRepositoryPort;
import com.example.account.modules.facturation.dto.request.BackOrderRequest;
import com.example.account.modules.facturation.dto.response.BackOrderResponse;
import com.example.account.modules.facturation.mapper.BackOrderMapper;
import com.example.account.modules.facturation.model.enums.StatutBackOrder;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackOrderUseCaseImpl implements BackOrderUseCase {

    private final BackOrderRepositoryPort backOrderRepository;
    private final BackOrderMapper backOrderMapper;
    private final DocPermissionService docPermissionService;

    private <T> Mono<T> grantOwnerPermission(UUID sellerId, UUID docId, T response) {
        if (sellerId == null || docId == null) return Mono.just(response);
        AssignDocPermissionRequest request = new AssignDocPermissionRequest();
        request.setSellerId(sellerId);
        request.setDocId(docId);
        request.setDocType(DocType.BACK_ORDER);
        request.setPermission(DocPermissionLevel.OWNER);
        return docPermissionService.grant(request)
                .thenReturn(response)
                .onErrorResume(e -> {
                    log.error("Failed to grant owner doc-permission for back-order {}: {}", docId, e.getMessage());
                    return Mono.just(response);
                });
    }

    @Override
    @Transactional
    public Mono<BackOrderResponse> createBackOrder(BackOrderRequest request) {
        log.info("Création d'un nouveau back-order");
        BackOrder entity = backOrderMapper.toEntity(request);
        if (entity.getIdBackOrder() == null) {
            entity.setIdBackOrder(UUID.randomUUID());
        }
        if (entity.getStatut() == null) {
            entity.setStatut(StatutBackOrder.EN_ATTENTE);
        }
        return backOrderRepository.insert(entity)
                .flatMap(savedEntity -> grantOwnerPermission(
                        savedEntity.getCreatedBy(),
                        savedEntity.getIdBackOrder(),
                        backOrderMapper.toResponse(savedEntity)));
    }

    @Override
    @Transactional
    public Mono<BackOrderResponse> updateBackOrder(UUID id, BackOrderRequest request) {
        log.info("Mise à jour du back-order: {}", id);
        return backOrderRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Back-order non trouvé: " + id)))
                .flatMap(entity -> {
                    UUID originalId = entity.getIdBackOrder();
                    UUID originalOrgId = entity.getOrganizationId();
                    backOrderMapper.updateEntityFromRequest(request, entity);
                    entity.setIdBackOrder(originalId);
                    if (entity.getOrganizationId() == null) {
                        entity.setOrganizationId(originalOrgId);
                    }
                    return backOrderRepository.save(entity);
                })
                .map(backOrderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BackOrderResponse> getBackOrderById(UUID id) {
        log.info("Récupération du back-order: {}", id);
        return backOrderRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Back-order non trouvé: " + id)))
                .map(backOrderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BackOrderResponse> getAllBackOrders() {
        log.info("Récupération de tous les back-orders");
        return backOrderRepository.findAll()
                .map(backOrderMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<BackOrderResponse> updateStatut(UUID id, StatutBackOrder statut) {
        log.info("Mise à jour du statut du back-order {} → {}", id, statut);
        return backOrderRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Back-order non trouvé: " + id)))
                .flatMap(entity -> {
                    entity.setStatut(statut);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return backOrderRepository.save(entity);
                })
                .map(backOrderMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> deleteBackOrder(UUID id) {
        log.info("Suppression du back-order: {}", id);
        return backOrderRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Back-order non trouvé: " + id)))
                .flatMap(backOrderRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BackOrderResponse> getByOrganizationId(UUID organizationId) {
        return backOrderRepository.findByOrganizationId(organizationId).map(backOrderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BackOrderResponse> getByAgencyId(UUID agencyId) {
        return backOrderRepository.findByAgencyId(agencyId).map(backOrderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BackOrderResponse> getByIdBonLivraison(UUID idBonLivraison) {
        return backOrderRepository.findByIdBonLivraison(idBonLivraison).map(backOrderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BackOrderResponse> getBySellerId(UUID sellerId) {
        return docPermissionService.findBySellerAndDocType(sellerId, DocType.BACK_ORDER)
                .flatMap(permission -> backOrderRepository.findById(permission.getDocId())
                        .map(entity -> {
                            BackOrderResponse response = backOrderMapper.toResponse(entity);
                            response.setDocPermission(docPermissionService.toResponse(permission));
                            return response;
                        }));
    }
}
