package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.AssignProducerRequest;
import com.example.account.modules.facturation.dto.response.ProducerAssignmentResponse;
import com.example.account.modules.facturation.service.ProducerAssignmentService;
import com.example.account.modules.tiers.dto.FournisseurResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/producer-assignments")
@RequiredArgsConstructor
@Tag(name = "Producer Assignments", description = "Assigning suppliers/producers to their account-manager seller")
public class ProducerAssignmentController {

    private final ProducerAssignmentService service;

    @PostMapping
    @Operation(summary = "Assign (or reassign) a producer to a seller")
    public Mono<ProducerAssignmentResponse> assign(@Valid @RequestBody AssignProducerRequest request) {
        return service.assign(request);
    }

    @GetMapping("/fournisseur/{fournisseurId}")
    @Operation(summary = "Get the current assignment for a producer")
    public Mono<ProducerAssignmentResponse> getForFournisseur(@PathVariable UUID fournisseurId, @RequestParam UUID organizationId) {
        return service.getForFournisseur(fournisseurId, organizationId);
    }

    @GetMapping("/organisation/{organizationId}")
    @Operation(summary = "List all producer assignments for an organization")
    public Flux<ProducerAssignmentResponse> listByOrganization(@PathVariable UUID organizationId) {
        return service.listByOrganization(organizationId);
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "List all producer assignments for a seller")
    public Flux<ProducerAssignmentResponse> listBySeller(@PathVariable UUID sellerId, @RequestParam UUID organizationId) {
        return service.listBySeller(sellerId, organizationId);
    }

    @GetMapping("/seller/{sellerId}/producers")
    @Operation(summary = "Get the producers/suppliers assigned to a seller")
    public Flux<FournisseurResponse> getProducersForSeller(@PathVariable UUID sellerId, @RequestParam UUID organizationId) {
        return service.getProducersForSeller(sellerId, organizationId);
    }

    @DeleteMapping("/fournisseur/{fournisseurId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a producer's seller assignment")
    public Mono<Void> unassign(@PathVariable UUID fournisseurId, @RequestParam UUID organizationId) {
        return service.unassign(fournisseurId, organizationId);
    }
}
