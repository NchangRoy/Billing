package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.domain.port.input.BackOrderUseCase;
import com.example.account.modules.facturation.dto.request.BackOrderRequest;
import com.example.account.modules.facturation.dto.response.BackOrderResponse;
import com.example.account.modules.facturation.model.enums.StatutBackOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facturation/back-orders")
@RequiredArgsConstructor
@Tag(name = "Back Order", description = "API de gestion des Back Orders")
public class BackOrderController {

    private final BackOrderUseCase backOrderUseCase;

    @PostMapping
    @Operation(summary = "Créer un back-order")
    public Mono<ResponseEntity<BackOrderResponse>> createBackOrder(@Valid @RequestBody BackOrderRequest request) {
        return backOrderUseCase.createBackOrder(request)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un back-order par ID")
    public Mono<ResponseEntity<BackOrderResponse>> getBackOrderById(@PathVariable UUID id) {
        return backOrderUseCase.getBackOrderById(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Lister tous les back-orders")
    public Flux<BackOrderResponse> getAllBackOrders() {
        return backOrderUseCase.getAllBackOrders();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un back-order")
    public Mono<ResponseEntity<BackOrderResponse>> updateBackOrder(@PathVariable UUID id,
                                                                    @RequestBody BackOrderRequest request) {
        return backOrderUseCase.updateBackOrder(id, request)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Changer le statut d'un back-order")
    public Mono<ResponseEntity<BackOrderResponse>> updateStatut(@PathVariable UUID id,
                                                                 @RequestParam StatutBackOrder statut) {
        return backOrderUseCase.updateStatut(id, statut)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un back-order")
    public Mono<ResponseEntity<Void>> deleteBackOrder(@PathVariable UUID id) {
        return backOrderUseCase.deleteBackOrder(id)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }

    @GetMapping("/organisation/{organizationId}")
    @Operation(summary = "Récupérer les back-orders par organisation")
    public Flux<BackOrderResponse> getByOrganizationId(@PathVariable UUID organizationId) {
        return backOrderUseCase.getByOrganizationId(organizationId);
    }

    @GetMapping("/agence/{agencyId}")
    @Operation(summary = "Récupérer les back-orders par agence")
    public Flux<BackOrderResponse> getByAgencyId(@PathVariable UUID agencyId) {
        return backOrderUseCase.getByAgencyId(agencyId);
    }

    @GetMapping("/bon-achat/{idBonAchat}")
    @Operation(summary = "Récupérer les back-orders par bon d'achat")
    public Flux<BackOrderResponse> getByIdBonAchat(@PathVariable UUID idBonAchat) {
        return backOrderUseCase.getByIdBonAchat(idBonAchat);
    }
}
