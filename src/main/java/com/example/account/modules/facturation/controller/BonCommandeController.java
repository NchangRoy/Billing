package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.BonCommandeCreateRequest;
import com.example.account.modules.facturation.dto.response.BonCommandeResponse;
import com.example.account.modules.facturation.model.enums.StatusBonCommande;
import com.example.account.modules.facturation.service.BonCommandeService;
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
@RequestMapping("/api/bon-commande")
@RequiredArgsConstructor
@Tag(name = "Bon Commande", description = "API de gestion des Bons de Commande (WebFlux)")
public class BonCommandeController {

    private final BonCommandeService bonCommandeService;

    @PostMapping
    @Operation(summary = "Créer un bon de commande")
    public Mono<ResponseEntity<BonCommandeResponse>> createBonCommande(@Valid @RequestBody BonCommandeCreateRequest request) {
        return bonCommandeService.createBonCommande(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un bon de commande")
    public Mono<ResponseEntity<BonCommandeResponse>> updateBonCommandeById(@PathVariable UUID id, @RequestBody BonCommandeCreateRequest request) {
        return bonCommandeService.updateBonCommande(id, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les bons de commande")
    public Flux<BonCommandeResponse> getAllBonCommandes() {
        return bonCommandeService.getAllBonCommandes();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un bon de commande par ID")
    public Mono<ResponseEntity<BonCommandeResponse>> getBonCommandeById(@PathVariable UUID id) {
        return bonCommandeService.getBonCommandeById(id)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Changer l'état d'un bon de commande")
    public Mono<ResponseEntity<BonCommandeResponse>> updateStatut(
            @PathVariable UUID id,
            @RequestParam StatusBonCommande statut) {
        return bonCommandeService.updateStatut(id, statut)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/organisation/{organizationId}")
    @Operation(summary = "Récupérer les bons de commande par organisation")
    public Flux<BonCommandeResponse> getByOrganizationId(@PathVariable UUID organizationId) {
        return bonCommandeService.getByOrganizationId(organizationId);
    }

    @GetMapping("/agence/{agencyId}")
    @Operation(summary = "Récupérer les bons de commande par agence")
    public Flux<BonCommandeResponse> getByAgencyId(@PathVariable UUID agencyId) {
        return bonCommandeService.getByAgencyId(agencyId);
    }
}
