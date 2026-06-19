package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.FactureCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureResponse;
import com.example.account.modules.facturation.model.enums.StatutFacture;
import com.example.account.modules.facturation.domain.port.input.FactureUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Facture", description = "API de gestion des factures (WebFlux)")
public class FactureController {

    private final FactureUseCase factureService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle facture")
    public Mono<ResponseEntity<FactureResponse>> createFacture(@Valid @RequestBody FactureCreateRequest request) {
        log.info("Requête de création de facture pour le client: {}", request.getIdClient());
        return factureService.createFacture(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{factureId}")
    @Operation(summary = "Mettre à jour une facture")
    public Mono<ResponseEntity<FactureResponse>> updateFacture(
            @PathVariable UUID factureId,
            @Valid @RequestBody FactureCreateRequest request) {
        log.info("Requête de mise à jour de la facture: {}", factureId);
        return factureService.updateFacture(factureId, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{factureId}")
    @Operation(summary = "Récupérer une facture par ID")
    public Mono<ResponseEntity<FactureResponse>> getFactureById(@PathVariable UUID factureId) {
        log.info("Requête de récupération de la facture: {}", factureId);
        return factureService.getFactureById(factureId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/account/{factureId}")
    @Operation(summary = "Comptabiliser une Facture")
    public Mono<ResponseEntity<Void>> accountFacture(@PathVariable UUID factureId) {
        log.info("Comptabiliser la facture: {}", factureId);
        return factureService.accountFacture(factureId)
                .thenReturn(ResponseEntity.ok().<Void>build())
                .onErrorResume(e -> {
                    log.error("Erreur lors de la comptabilisation: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
                });
    }

    @GetMapping("/numero/{numeroFacture}")
    @Operation(summary = "Récupérer une facture par numéro")
    public Mono<ResponseEntity<FactureResponse>> getFactureByNumero(@PathVariable String numeroFacture) {
        log.info("Requête de récupération de la facture par numéro: {}", numeroFacture);
        return factureService.getFactureByNumero(numeroFacture)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Récupérer toutes les factures")
    public Flux<FactureResponse> getAllFactures() {
        log.info("Requête de récupération de toutes les factures");
        return factureService.getAllFactures();
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Récupérer les factures d'un client")
    public Flux<FactureResponse> getFacturesByClient(@PathVariable UUID clientId) {
        log.info("Requête de récupération des factures du client: {}", clientId);
        return factureService.getFacturesByClient(clientId);
    }

    @GetMapping("/etat/{etat}")
    @Operation(summary = "Récupérer les factures par état")
    public Flux<FactureResponse> getFacturesByEtat(@PathVariable StatutFacture etat) {
        log.info("Requête de récupération des factures par état: {}", etat);
        return factureService.getFacturesByEtat(etat);
    }

    @GetMapping("/retard")
    @Operation(summary = "Récupérer les factures en retard")
    public Flux<FactureResponse> getFacturesEnRetard() {
        log.info("Requête de récupération des factures en retard");
        return factureService.getFacturesEnRetard();
    }

    @GetMapping("/non-payees")
    @Operation(summary = "Récupérer les factures non payées")
    public Flux<FactureResponse> getFacturesNonPayees() {
        log.info("Requête de récupération des factures non payées");
        return factureService.getFacturesNonPayees();
    }

    @GetMapping("/periode")
    @Operation(summary = "Récupérer les factures par période")
    public Flux<FactureResponse> getFacturesByPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        log.info("Requête de récupération des factures entre {} et {}", dateDebut, dateFin);
        return factureService.getFacturesByPeriode(dateDebut, dateFin);
    }

    @DeleteMapping("/{factureId}")
    @Operation(summary = "Supprimer une facture")
    public Mono<ResponseEntity<Void>> deleteFacture(@PathVariable UUID factureId) {
        log.info("Requête de suppression de la facture: {}", factureId);
        return factureService.deleteFacture(factureId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PutMapping("/{factureId}/marquer-paye")
    @Operation(summary = "Marquer une facture comme payée")
    public Mono<ResponseEntity<FactureResponse>> marquerCommePaye(@PathVariable UUID factureId) {
        log.info("Requête de marquage de la facture {} comme payée", factureId);
        return factureService.marquerCommePaye(factureId)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{factureId}/paiement")
    @Operation(summary = "Enregistrer un paiement pour une facture")
    public Mono<ResponseEntity<FactureResponse>> enregistrerPaiement(
            @PathVariable UUID factureId,
            @RequestParam BigDecimal montantPaye) {
        log.info("Requête d'enregistrement d'un paiement de {} pour la facture {}", montantPaye, factureId);
        return factureService.enregistrerPaiement(factureId, montantPaye)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/count/etat/{etat}")
    @Operation(summary = "Compter les factures par état")
    public Mono<ResponseEntity<Long>> countByEtat(@PathVariable StatutFacture etat) {
        log.info("Requête de comptage des factures par état: {}", etat);
        return factureService.countByEtat(etat)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/organisation/{organizationId}")
    @Operation(summary = "Récupérer les factures par organisation")
    public Flux<FactureResponse> getFacturesByOrganizationId(@PathVariable UUID organizationId) {
        log.info("Requête de récupération des factures par organisation: {}", organizationId);
        return factureService.getFacturesByOrganizationId(organizationId);
    }

    @GetMapping("/agence/{agencyId}")
    @Operation(summary = "Récupérer les factures par agence")
    public Flux<FactureResponse> getFacturesByAgencyId(@PathVariable UUID agencyId) {
        log.info("Requête de récupération des factures par agence: {}", agencyId);
        return factureService.getFacturesByAgencyId(agencyId);
    }

}
