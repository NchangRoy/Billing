package com.example.account.modules.core.controller;

import com.example.account.modules.core.domain.port.input.AgencyUseCase;
import com.example.account.modules.core.dto.AgencyCreateRequest;
import com.example.account.modules.shared.dto.kernel.KernelAgencyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/agencies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Agencies", description = "Agencies, read through from Kernel (sales-core has no own agency endpoint)")
public class AgencyController {

    private final AgencyUseCase agencyUseCase;

    @GetMapping
    @Operation(summary = "List agencies for an organization")
    public Flux<KernelAgencyResponse> getAll(@RequestParam UUID organizationId) {
        return agencyUseCase.findAllByOrganization(organizationId);
    }

    @GetMapping("/{agencyId}")
    @Operation(summary = "Get an agency by ID")
    public Mono<KernelAgencyResponse> getById(@PathVariable UUID agencyId, @RequestParam UUID organizationId) {
        return agencyUseCase.findById(organizationId, agencyId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an agency for an organization")
    public Mono<KernelAgencyResponse> create(@RequestParam UUID organizationId, @Valid @RequestBody AgencyCreateRequest request) {
        return agencyUseCase.create(organizationId, request);
    }
}
