package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.DevisCreateRequest;
import com.example.account.modules.facturation.dto.request.ExternalRequest.EmailRequest;
import com.example.account.modules.facturation.dto.response.DevisResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.EnrichedDevisResponse;
import com.example.account.modules.facturation.model.enums.StatutDevis;
import com.example.account.modules.facturation.service.DevisService;
import com.example.account.modules.facturation.service.Journals.DevisJournalService;
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

import java.time.LocalDate;
import java.util.UUID;


@RestController
@RequestMapping("/api/devis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Devis", description = "API de gestion des devis (WebFlux)")
public class DevisController {

    private final DevisService devisService;
    private final DevisJournalService devisJournalService;

    @PostMapping
    @Operation(summary = "Créer un nouveau devis")
    public Mono<ResponseEntity<DevisResponse>> createDevis( @RequestBody DevisCreateRequest request) {
        System.out.println("Creating Devis \n\n");
        log.info("Requête de création de devis pour le client: {}", request.getIdClient());
        return devisService.createDevis(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping("/enriched/{orgId}")
    @Operation(summary = "Enrichir les devis")
    public Flux<EnrichedDevisResponse> getEnrichedDevis(@PathVariable UUID orgId) {
        log.info("Requête de récupération des devis enrichis pour l'organisation: {}", orgId);
        return devisJournalService.enrichDevis(orgId);
    }

    @PutMapping("/{devisId}")
    @Operation(summary = "Mettre à jour un devis")
    public Mono<ResponseEntity<DevisResponse>> updateDevis(
            @PathVariable UUID devisId,
            @Valid @RequestBody DevisCreateRequest request) {
        log.info("Requête de mise à jour du devis: {}", devisId);
        return devisService.updateDevis(devisId, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{devisId}")
    @Operation(summary = "Récupérer un devis par ID")
    public Mono<ResponseEntity<DevisResponse>> getDevisById(@PathVariable UUID devisId) {
        log.info("Requête de récupération du devis: {}", devisId);
        return devisService.getDevisById(devisId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/numero/{numeroDevis}")
    @Operation(summary = "Récupérer un devis par numéro")
    public Mono<ResponseEntity<DevisResponse>> getDevisByNumero(@PathVariable String numeroDevis) {
        log.info("Requête de récupération du devis par numéro: {}", numeroDevis);
        return devisService.getDevisByNumero(numeroDevis)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les devis")
    public Flux<DevisResponse> getAllDevis() {
        log.info("Requête de récupération de tous les devis");
        return devisService.getAllDevis();
    }

@PostMapping("/email")
public Mono<ResponseEntity<Void>> sendQuotationEmail(@RequestBody EmailRequest request) {
    log.info("Received request to send quotation email for ID: {}", request.getId());
    
    return devisService.sendDevisAsEmail(request)
        .then(Mono.just(ResponseEntity.ok().<Void>build()))
        .onErrorResume(e -> {
            log.error("Failed to send email for quotation: " + request.getId(), e);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        });
}

   


    

    @DeleteMapping("/{devisId}")
    @Operation(summary = "Supprimer un devis")
    public Mono<ResponseEntity<Void>> deleteDevis(@PathVariable UUID devisId) {
        log.info("Requête de suppression du devis: {}", devisId);
        return devisService.deleteDevis(devisId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PutMapping("/{devisId}/accepter")
    @Operation(summary = "Accepter un devis")
    public Mono<ResponseEntity<Void>> accepterDevis(@PathVariable UUID devisId) {
        log.info("Requête d'acceptation du devis: {}", devisId);
        return devisService.accepterDevis(devisId)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{devisId}/refuser")
    @Operation(summary = "Refuser un devis")
    public Mono<ResponseEntity<Void>> refuserDevis(
            @PathVariable UUID devisId
            ) {
        log.info("Requête de refus du devis: {}", devisId);
        return devisService.refuserDevis(devisId)
                .map(ResponseEntity::ok);
    }
}
