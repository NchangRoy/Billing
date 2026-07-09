package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.QuotationProposalDecisionRequest;
import com.example.account.modules.facturation.dto.response.QuotationProposalResponse;
import com.example.account.modules.facturation.service.QuotationProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/quotation-proposals")
@RequiredArgsConstructor
@Tag(name = "Quotation Proposals", description = "Quotations drafted by clients themselves, reviewed by sellers")
public class QuotationProposalController {

    private final QuotationProposalService service;

    @GetMapping
    @Operation(summary = "Lister toutes les propositions de devis")
    public Flux<QuotationProposalResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une proposition par ID")
    public Mono<QuotationProposalResponse> getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @GetMapping("/organisation/{organizationId}")
    @Operation(summary = "Récupérer les propositions par organisation")
    public Flux<QuotationProposalResponse> getByOrganizationId(@PathVariable UUID organizationId) {
        return service.getByOrganizationId(organizationId);
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Accepter une proposition de devis")
    public Mono<QuotationProposalResponse> accept(@PathVariable UUID id, @RequestBody(required = false) QuotationProposalDecisionRequest request) {
        return service.accept(id, request != null ? request.getCommentary() : null);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Refuser une proposition de devis")
    public Mono<QuotationProposalResponse> reject(@PathVariable UUID id, @RequestBody(required = false) QuotationProposalDecisionRequest request) {
        return service.reject(id, request != null ? request.getCommentary() : null);
    }
}
