package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.domain.port.input.UIPermissionsUseCase;
import com.example.account.modules.facturation.dto.request.UIPermissionsRequest;
import com.example.account.modules.facturation.dto.response.UIPermissionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ui-permissions")
@RequiredArgsConstructor
@Tag(name = "UI Permissions", description = "Gestion des permissions d'interface par vendeur")
public class UIPermissionsController {

    private final UIPermissionsUseCase useCase;

    @PostMapping
    @Operation(summary = "Créer des permissions UI pour un vendeur")
    public Mono<ResponseEntity<UIPermissionsResponse>> create(@RequestBody UIPermissionsRequest request) {
        return useCase.create(request)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer des permissions UI par ID")
    public Mono<ResponseEntity<UIPermissionsResponse>> getById(@PathVariable UUID id) {
        return useCase.getById(id).map(ResponseEntity::ok);
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Récupérer les permissions UI d'un vendeur")
    public Flux<UIPermissionsResponse> getBySellerId(@PathVariable UUID sellerId) {
        return useCase.getBySellerId(sellerId);
    }

    @GetMapping("/organisation/{organizationId}")
    @Operation(summary = "Récupérer les permissions UI par organisation")
    public Flux<UIPermissionsResponse> getByOrganizationId(@PathVariable UUID organizationId) {
        return useCase.getByOrganizationId(organizationId);
    }

    @GetMapping("/agence/{agencyId}")
    @Operation(summary = "Récupérer les permissions UI par agence")
    public Flux<UIPermissionsResponse> getByAgencyId(@PathVariable UUID agencyId) {
        return useCase.getByAgencyId(agencyId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour les permissions UI")
    public Mono<ResponseEntity<UIPermissionsResponse>> update(@PathVariable UUID id,
                                                               @RequestBody UIPermissionsRequest request) {
        return useCase.update(id, request).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer des permissions UI")
    public Mono<ResponseEntity<Void>> delete(@PathVariable UUID id) {
        return useCase.delete(id).thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
