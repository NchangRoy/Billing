package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.TaxeCreateRequest;
import com.example.account.modules.facturation.dto.request.TaxeUpdateRequest;
import com.example.account.modules.facturation.dto.response.TaxeResponse;
import com.example.account.modules.facturation.domain.port.input.TaxeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/taxes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Taxe", description = "API de gestion des taxes (WebFlux)")
public class TaxeController {

    private final TaxeUseCase taxeService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle taxe")
    public Mono<ResponseEntity<TaxeResponse>> createTaxe(@Valid @RequestBody TaxeCreateRequest request) {
        log.info("Requête de création de taxe: {}", request.getNomTaxe());
        return taxeService.createTaxe(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{taxeId}")
    @Operation(summary = "Mettre à jour une taxe")
    public Mono<ResponseEntity<TaxeResponse>> updateTaxe(
            @PathVariable UUID taxeId,
            @Valid @RequestBody TaxeUpdateRequest request) {
        log.info("Requête de mise à jour de la taxe: {}", taxeId);
        return taxeService.updateTaxe(taxeId, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{taxeId}")
    @Operation(summary = "Récupérer une taxe par ID")
    public Mono<ResponseEntity<TaxeResponse>> getTaxeById(@PathVariable UUID taxeId) {
        log.info("Requête de récupération de la taxe: {}", taxeId);
        return taxeService.getTaxeById(taxeId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/nom/{nomTaxe}")
    @Operation(summary = "Récupérer une taxe par nom")
    public Mono<ResponseEntity<TaxeResponse>> getTaxeByNom(@PathVariable String nomTaxe) {
        log.info("Requête de récupération de la taxe par nom: {}", nomTaxe);
        return taxeService.getTaxeByNom(nomTaxe)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Récupérer toutes les taxes")
    public Flux<TaxeResponse> getAllTaxes() {
        log.info("Requête de récupération de toutes les taxes");
        return taxeService.getAllTaxes();
    }

    @GetMapping("/actives")
    @Operation(summary = "Récupérer toutes les taxes actives")
    public Flux<TaxeResponse> getActiveTaxes() {
        log.info("Requête de récupération des taxes actives");
        return taxeService.getActiveTaxes();
    }

    @GetMapping("/type/{typeTaxe}")
    @Operation(summary = "Récupérer les taxes par type")
    public Flux<TaxeResponse> getTaxesByType(@PathVariable String typeTaxe) {
        log.info("Requête de récupération des taxes par type: {}", typeTaxe);
        return taxeService.getTaxesByType(typeTaxe);
    }

    @GetMapping("/type/{typeTaxe}/actives")
    @Operation(summary = "Récupérer les taxes actives par type")
    public Flux<TaxeResponse> getActiveTaxesByType(@PathVariable String typeTaxe) {
        log.info("Requête de récupération des taxes actives par type: {}", typeTaxe);
        return taxeService.getActiveTaxesByType(typeTaxe);
    }

    @GetMapping("/porte/{porteTaxe}")
    @Operation(summary = "Récupérer les taxes par portée")
    public Flux<TaxeResponse> getTaxesByPorte(@PathVariable String porteTaxe) {
        log.info("Requête de récupération des taxes par portée: {}", porteTaxe);
        return taxeService.getTaxesByPorte(porteTaxe);
    }

    @GetMapping("/position/{positionFiscale}")
    @Operation(summary = "Récupérer les taxes par position fiscale")
    public Flux<TaxeResponse> getTaxesByPositionFiscale(@PathVariable String positionFiscale) {
        log.info("Requête de récupération des taxes par position fiscale: {}", positionFiscale);
        return taxeService.getTaxesByPositionFiscale(positionFiscale);
    }

    @GetMapping("/calcul-range")
    @Operation(summary = "Récupérer les taxes par plage de calcul")
    public Flux<TaxeResponse> getTaxesByCalculRange(
            @RequestParam BigDecimal minTaux,
            @RequestParam BigDecimal maxTaux) {
        log.info("Requête de récupération des taxes par plage de calcul: {} - {}", minTaux, maxTaux);
        return taxeService.getTaxesByCalculRange(minTaux, maxTaux);
    }

    @GetMapping("/montant-range")
    @Operation(summary = "Récupérer les taxes par plage de montant")
    public Flux<TaxeResponse> getTaxesByMontantRange(
            @RequestParam BigDecimal minMontant,
            @RequestParam BigDecimal maxMontant) {
        log.info("Requête de récupération des taxes par plage de montant: {} - {}", minMontant, maxMontant);
        return taxeService.getTaxesByMontantRange(minMontant, maxMontant);
    }

    @DeleteMapping("/{taxeId}")
    @Operation(summary = "Supprimer une taxe")
    public Mono<ResponseEntity<Void>> deleteTaxe(@PathVariable UUID taxeId) {
        log.info("Requête de suppression de la taxe: {}", taxeId);
        return taxeService.deleteTaxe(taxeId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PutMapping("/{taxeId}/activer")
    @Operation(summary = "Activer une taxe")
    public Mono<ResponseEntity<TaxeResponse>> activerTaxe(@PathVariable UUID taxeId) {
        log.info("Requête d'activation de la taxe: {}", taxeId);
        return taxeService.activerTaxe(taxeId)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{taxeId}/desactiver")
    @Operation(summary = "Désactiver une taxe")
    public Mono<ResponseEntity<TaxeResponse>> desactiverTaxe(@PathVariable UUID taxeId) {
        log.info("Requête de désactivation de la taxe: {}", taxeId);
        return taxeService.desactiverTaxe(taxeId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/count/actives")
    @Operation(summary = "Compter les taxes actives")
    public Mono<ResponseEntity<Long>> countActiveTaxes() {
        log.info("Requête de comptage des taxes actives");
        return taxeService.countActiveTaxes()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/count/type/{typeTaxe}")
    @Operation(summary = "Compter les taxes par type")
    public Mono<ResponseEntity<Long>> countByType(@PathVariable String typeTaxe) {
        log.info("Requête de comptage des taxes par type: {}", typeTaxe);
        return taxeService.countByType(typeTaxe)
                .map(ResponseEntity::ok);
    }
}
