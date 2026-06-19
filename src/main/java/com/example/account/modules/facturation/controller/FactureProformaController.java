package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.ProformaInvoiceRequest;
import com.example.account.modules.facturation.dto.response.ProformaInvoiceResponse;
import com.example.account.modules.facturation.model.enums.StatutProforma;
import com.example.account.modules.facturation.domain.port.input.FactureProformaUseCase;
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
@RequestMapping("/api/factures-proforma")
@RequiredArgsConstructor
@Tag(name = "Factures Proforma", description = "API de gestion des Factures Proforma (WebFlux)")
public class FactureProformaController {

    private final FactureProformaUseCase proformaService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle facture proforma")
    public Mono<ResponseEntity<ProformaInvoiceResponse>> createProforma(@Valid @RequestBody ProformaInvoiceRequest request) {
        return proformaService.createProforma(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une facture proforma par ID")
    public Mono<ResponseEntity<ProformaInvoiceResponse>> getProformaById(@PathVariable UUID id) {
        return proformaService.getProformaById(id)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une facture proforma")
    public Mono<ResponseEntity<ProformaInvoiceResponse>> updateFactureProforma(@PathVariable UUID id, @RequestBody ProformaInvoiceRequest request) {
        return proformaService.updateFactureProforma(id, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les factures proforma")
    public Flux<ProformaInvoiceResponse> getAllProformas() {
        return proformaService.getAllProformas();
    }

    @GetMapping("/client/{idClient}")
    @Operation(summary = "Lister les factures proforma par client")
    public Flux<ProformaInvoiceResponse> getProformasByClient(@PathVariable UUID idClient) {
        return proformaService.getProformasByClient(idClient);
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Mettre à jour le statut d'une facture proforma")
    public Mono<ResponseEntity<ProformaInvoiceResponse>> updateStatut(
            @PathVariable UUID id,
            @RequestParam StatutProforma statut) {
        return proformaService.updateStatut(id, statut)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une facture proforma")
    public Mono<ResponseEntity<Void>> deleteProforma(@PathVariable UUID id) {
        return proformaService.deleteProforma(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/organisation/{organizationId}")
    @Operation(summary = "Récupérer les factures proforma par organisation")
    public Flux<ProformaInvoiceResponse> getProformasByOrganizationId(@PathVariable UUID organizationId) {
        return proformaService.getProformasByOrganizationId(organizationId);
    }

    @GetMapping("/agence/{agencyId}")
    @Operation(summary = "Récupérer les factures proforma par agence")
    public Flux<ProformaInvoiceResponse> getProformasByAgencyId(@PathVariable UUID agencyId) {
        return proformaService.getProformasByAgencyId(agencyId);
    }
}
