package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.PaiementCreateRequest;
import com.example.account.modules.facturation.dto.request.PaiementUpdateRequest;
import com.example.account.modules.facturation.dto.response.PaiementResponse;
import com.example.account.modules.facturation.domain.port.input.PaiementUseCase;
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
@RequestMapping("/api/paiement")
@RequiredArgsConstructor
@Tag(name = "Paiement", description = "API de gestion des paiements (WebFlux)")
public class PaiementController {

    private final PaiementUseCase paiementService;

    @PostMapping
    @Operation(summary = "Créer un nouveau paiement")
    public Mono<ResponseEntity<PaiementResponse>> createPaiement(@Valid @RequestBody PaiementCreateRequest request) {
        return paiementService.createPaiement(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un paiement")
    public Mono<ResponseEntity<PaiementResponse>> updatePaiement(
            @PathVariable UUID id,
            @Valid @RequestBody PaiementUpdateRequest request) {
        return paiementService.updatePaiement(id, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les paiements")
    public Flux<PaiementResponse> getAllPaiements() {
        return paiementService.getAllPaiements();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un paiement par ID")
    public Mono<ResponseEntity<PaiementResponse>> getPaiementById(@PathVariable UUID id) {
        return paiementService.getPaiementById(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Récupérer les paiements d'un client")
    public Flux<PaiementResponse> getPaiementsByClient(@PathVariable UUID clientId) {
        return paiementService.getPaiementsByClient(clientId);
    }

    @GetMapping("/facture/{factureId}")
    @Operation(summary = "Récupérer les paiements d'une facture")
    public Flux<PaiementResponse> getPaiementsByFacture(@PathVariable UUID factureId) {
        return paiementService.getPaiementsByFacture(factureId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un paiement")
    public Mono<ResponseEntity<Void>> deletePaiement(@PathVariable UUID id) {
        return paiementService.deletePaiement(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/organisation/{organizationId}")
    @Operation(summary = "Récupérer les paiements par organisation")
    public Flux<PaiementResponse> getPaiementsByOrganizationId(@PathVariable UUID organizationId) {
        return paiementService.getPaiementsByOrganizationId(organizationId);
    }

    @GetMapping("/agence/{agencyId}")
    @Operation(summary = "Récupérer les paiements par agence")
    public Flux<PaiementResponse> getPaiementsByAgencyId(@PathVariable UUID agencyId) {
        return paiementService.getPaiementsByAgencyId(agencyId);
    }
}
