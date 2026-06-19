package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.JournalCreateRequest;
import com.example.account.modules.facturation.dto.request.JournalUpdateRequest;
import com.example.account.modules.facturation.dto.response.JournalResponse;
import com.example.account.modules.facturation.domain.port.input.JournalUseCase;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/journals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Journal", description = "API de gestion des journaux (WebFlux)")
public class JournalController {

    private final JournalUseCase journalService;

    @PostMapping
    @Operation(summary = "Créer un nouveau journal")
    public Mono<ResponseEntity<JournalResponse>> createJournal(@Valid @RequestBody JournalCreateRequest request) {
        log.info("Requête de création de journal: {}", request.getNomJournal());
        return journalService.createJournal(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{journalId}")
    @Operation(summary = "Mettre à jour un journal")
    public Mono<ResponseEntity<JournalResponse>> updateJournal(
            @PathVariable UUID journalId,
            @Valid @RequestBody JournalUpdateRequest request) {
        log.info("Requête de mise à jour du journal: {}", journalId);
        return journalService.updateJournal(journalId, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{journalId}")
    @Operation(summary = "Récupérer un journal par ID")
    public Mono<ResponseEntity<JournalResponse>> getJournalById(@PathVariable UUID journalId) {
        log.info("Requête de récupération du journal: {}", journalId);
        return journalService.getJournalById(journalId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/nom/{nomJournal}")
    @Operation(summary = "Récupérer un journal par nom")
    public Mono<ResponseEntity<JournalResponse>> getJournalByNom(@PathVariable String nomJournal) {
        log.info("Requête de récupération du journal par nom: {}", nomJournal);
        return journalService.getJournalByNom(nomJournal)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les journaux")
    public Flux<JournalResponse> getAllJournals() {
        log.info("Requête de récupération de tous les journaux");
        return journalService.getAllJournals();
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Récupérer les journaux par type")
    public Flux<JournalResponse> getJournalsByType(@PathVariable String type) {
        log.info("Requête de récupération des journaux par type: {}", type);
        return journalService.getJournalsByType(type);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des journaux par nom")
    public Flux<JournalResponse> searchJournalsByNom(@RequestParam String nom) {
        log.info("Requête de recherche des journaux par nom: {}", nom);
        return journalService.searchJournalsByNom(nom);
    }

    @DeleteMapping("/{journalId}")
    @Operation(summary = "Supprimer un journal")
    public Mono<ResponseEntity<Void>> deleteJournal(@PathVariable UUID journalId) {
        log.info("Requête de suppression du journal: {}", journalId);
        return journalService.deleteJournal(journalId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/count/type/{type}")
    @Operation(summary = "Compter les journaux par type")
    public Mono<ResponseEntity<Long>> countByType(@PathVariable String type) {
        log.info("Requête de comptage des journaux par type: {}", type);
        return journalService.countByType(type)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/organisation/{organizationId}")
    @Operation(summary = "Récupérer les journaux par organisation")
    public Flux<JournalResponse> getJournalsByOrganizationId(@PathVariable UUID organizationId) {
        log.info("Requête de récupération des journaux par organisation: {}", organizationId);
        return journalService.getJournalsByOrganizationId(organizationId);
    }

    @GetMapping("/agence/{agencyId}")
    @Operation(summary = "Récupérer les journaux par agence")
    public Flux<JournalResponse> getJournalsByAgencyId(@PathVariable UUID agencyId) {
        log.info("Requête de récupération des journaux par agence: {}", agencyId);
        return journalService.getJournalsByAgencyId(agencyId);
    }
}
