package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.BonLivraisonRequest;
import com.example.account.modules.facturation.dto.response.BonLivraisonResponse;
import com.example.account.modules.facturation.model.enums.StatutBonLivraison;
import com.example.account.modules.facturation.service.BonLivraisonService;
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
@RequestMapping("/api/bons-livraison")
@RequiredArgsConstructor
@Tag(name = "Bon de livraison", description = "API de gestion des Bons de livraison (Delivery Notes) - WebFlux")
public class BonLivraisonController {

    private final BonLivraisonService bonLivraisonService;

    @PostMapping
    @Operation(summary = "Créer un nouveau bon de livraison")
    public Mono<ResponseEntity<BonLivraisonResponse>> createBonLivraison(@Valid @RequestBody BonLivraisonRequest request) {
        return bonLivraisonService.createBonLivraison(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un bon de livraison par ID")
    public Mono<ResponseEntity<BonLivraisonResponse>> getBonLivraisonById(@PathVariable UUID id) {
        return bonLivraisonService.getBonLivraisonById(id)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un bon de livraison par ID")
    public Mono<ResponseEntity<BonLivraisonResponse>> updatedLivraison(@PathVariable UUID id, @RequestBody BonLivraisonRequest request) {
        System.out.println(request);
        return bonLivraisonService.update(id, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Lister tous les bons de livraison")
    public Flux<BonLivraisonResponse> getAllBonLivraisons() {
        return bonLivraisonService.getAllBonLivraisons();
    }

    @GetMapping("/client/{idClient}")
    @Operation(summary = "Lister les bons de livraison par client")
    public Flux<BonLivraisonResponse> getBonLivraisonsByClient(@PathVariable UUID idClient) {
        return bonLivraisonService.getBonLivraisonsByClient(idClient);
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Mettre à jour le statut d'un bon de livraison")
    public Mono<ResponseEntity<BonLivraisonResponse>> updateStatut(
            @PathVariable UUID id,
            @RequestParam StatutBonLivraison statut) {
        return bonLivraisonService.updateStatut(id, statut)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/effectuer")
    @Operation(summary = "Marquer une livraison comme effectuée")
    public Mono<ResponseEntity<BonLivraisonResponse>> marquerCommeEffectuee(@PathVariable UUID id) {
        return bonLivraisonService.marquerCommeEffectuee(id)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un bon de livraison")
    public Mono<ResponseEntity<Void>> deleteBonLivraison(@PathVariable UUID id) {
        return bonLivraisonService.deleteBonLivraison(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/organisation/{organizationId}")
    @Operation(summary = "Récupérer les bons de livraison par organisation")
    public Flux<BonLivraisonResponse> getByOrganizationId(@PathVariable UUID organizationId) {
        return bonLivraisonService.getByOrganizationId(organizationId);
    }

    @GetMapping("/agence/{agencyId}")
    @Operation(summary = "Récupérer les bons de livraison par agence")
    public Flux<BonLivraisonResponse> getByAgencyId(@PathVariable UUID agencyId) {
        return bonLivraisonService.getByAgencyId(agencyId);
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Récupérer les bons de livraison créés par un vendeur")
    public Flux<BonLivraisonResponse> getBySellerId(@PathVariable UUID sellerId) {
        return bonLivraisonService.getBySellerId(sellerId);
    }
}
