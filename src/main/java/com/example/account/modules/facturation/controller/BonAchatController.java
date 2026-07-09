package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.BonAchatRequest;
import com.example.account.modules.facturation.dto.response.BonAchatResponse;
import com.example.account.modules.facturation.service.BonAchatService;
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
@RequestMapping("/api/bons-achat")
@RequiredArgsConstructor
@Tag(name = "Bon d'achat", description = "API de gestion des Bons d'achat (Goods Receipt Notes) - WebFlux")
public class BonAchatController {

    private final BonAchatService bonAchatService;

    @PostMapping
    @Operation(summary = "Créer un nouveau bon d'achat")
    public Mono<ResponseEntity<BonAchatResponse>> createBonAchat(@Valid @RequestBody BonAchatRequest request) {
        return bonAchatService.createBonAchat(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un bon d'achat par ID")
    public Mono<ResponseEntity<BonAchatResponse>> getBonAchatById(@PathVariable UUID id) {
        return bonAchatService.getBonAchatById(id)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update un bon d'achat par ID")
    public Mono<ResponseEntity<BonAchatResponse>> updateBonAchatById(@PathVariable UUID id, @RequestBody BonAchatRequest request) {
        return bonAchatService.updateBonAchat(id, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Lister tous les bons d'achat")
    public Flux<BonAchatResponse> getAllBonsAchat() {
        return bonAchatService.getAllBonsAchat();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un bon d'achat")
    public Mono<ResponseEntity<Void>> deleteBonAchat(@PathVariable UUID id) {
        return bonAchatService.deleteBonAchat(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/organisation/{organizationId}")
    @Operation(summary = "Récupérer les bons d'achat par organisation")
    public Flux<BonAchatResponse> getByOrganizationId(@PathVariable UUID organizationId) {
        return bonAchatService.getByOrganizationId(organizationId);
    }

    @GetMapping("/agence/{agencyId}")
    @Operation(summary = "Récupérer les bons d'achat par agence")
    public Flux<BonAchatResponse> getByAgencyId(@PathVariable UUID agencyId) {
        return bonAchatService.getByAgencyId(agencyId);
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Récupérer les bons d'achat créés par un vendeur")
    public Flux<BonAchatResponse> getBySellerId(@PathVariable UUID sellerId) {
        return bonAchatService.getBySellerId(sellerId);
    }

    @PutMapping("/{id}/accepter")
    @Operation(summary = "Accepter un bon d'achat")
    public Mono<ResponseEntity<Void>> accepterBonAchat(@PathVariable UUID id) {
        return bonAchatService.accepterBonAchat(id)
                .thenReturn(ResponseEntity.ok().build());
    }

    @PutMapping("/{id}/refuser")
    @Operation(summary = "Refuser un bon d'achat")
    public Mono<ResponseEntity<Void>> refuserBonAchat(@PathVariable UUID id) {
        return bonAchatService.refuserBonAchat(id)
                .thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/{id}/send-to-portal")
    @Operation(summary = "Envoyer le bon d'achat au fournisseur via le portail (login requis)")
    public Mono<ResponseEntity<Void>> sendToPortal(@PathVariable UUID id) {
        return bonAchatService.sendToPortal(id)
                .thenReturn(ResponseEntity.ok().build());
    }
}
