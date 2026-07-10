package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.FactureFournisseurCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureFournisseurResponse;
import com.example.account.modules.facturation.service.FactureFournisseurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/facture-fournisseurs")
@RequiredArgsConstructor
@Slf4j
public class FactureFournisseurController {

    private final FactureFournisseurService factureFournisseurService;

    @GetMapping
    public Flux<FactureFournisseurResponse> getFactures() {
        return factureFournisseurService.getAllFactures();
    }
    
    @PostMapping
    public Mono<ResponseEntity<FactureFournisseurResponse>> createFacture(@RequestBody FactureFournisseurCreateRequest dto) {
        return factureFournisseurService.createFacture(dto)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<FactureFournisseurResponse>> updateFacture(
        @PathVariable UUID id, 
        @RequestBody FactureFournisseurCreateRequest updatedData) {
    
        return factureFournisseurService.updateFacture(id, updatedData)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Erreur lors de la mise à jour de la facture fournisseur {}: {}", id, e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    @GetMapping("/organisation/{organizationId}")
    public Flux<FactureFournisseurResponse> getByOrganizationId(@PathVariable UUID organizationId) {
        return factureFournisseurService.getByOrganizationId(organizationId);
    }

    @GetMapping("/agence/{agencyId}")
    public Flux<FactureFournisseurResponse> getByAgencyId(@PathVariable UUID agencyId) {
        return factureFournisseurService.getByAgencyId(agencyId);
    }

    @GetMapping("/seller/{sellerId}")
    public Flux<FactureFournisseurResponse> getBySellerId(@PathVariable UUID sellerId) {
        return factureFournisseurService.getBySellerId(sellerId);
    }

    @GetMapping("/account/{id}")
    public Mono<ResponseEntity<Void>> accountFacture(@PathVariable UUID id) {
        log.info("Comptabiliser la facture fournisseur: {}", id);
        return factureFournisseurService.accountFacture(id)
                .thenReturn(ResponseEntity.ok().<Void>build())
                .onErrorResume(e -> {
                    log.error("Erreur lors de la comptabilisation: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    @PutMapping("/{id}/accounted")
    public Mono<ResponseEntity<Void>> markAccounted(@PathVariable UUID id) {
        log.info("Marquage de la facture fournisseur {} comme comptabilisée", id);
        return factureFournisseurService.markAccounted(id)
                .thenReturn(ResponseEntity.ok().<Void>build());
    }
}