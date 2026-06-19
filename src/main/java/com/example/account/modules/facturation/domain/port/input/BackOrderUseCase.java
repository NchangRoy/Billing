package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.BackOrderRequest;
import com.example.account.modules.facturation.dto.response.BackOrderResponse;
import com.example.account.modules.facturation.model.enums.StatutBackOrder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BackOrderUseCase {
    Mono<BackOrderResponse> createBackOrder(BackOrderRequest request);
    Mono<BackOrderResponse> updateBackOrder(UUID id, BackOrderRequest request);
    Mono<BackOrderResponse> getBackOrderById(UUID id);
    Flux<BackOrderResponse> getAllBackOrders();
    Mono<BackOrderResponse> updateStatut(UUID id, StatutBackOrder statut);
    Mono<Void> deleteBackOrder(UUID id);
    Flux<BackOrderResponse> getByOrganizationId(UUID organizationId);
    Flux<BackOrderResponse> getByAgencyId(UUID agencyId);
    Flux<BackOrderResponse> getByIdBonAchat(UUID idBonAchat);
}
