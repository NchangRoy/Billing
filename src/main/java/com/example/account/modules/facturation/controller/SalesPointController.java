package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.domain.port.input.SalesPointUseCase;
import com.example.account.modules.facturation.dto.request.CreateSalesPointRequest;
import com.example.account.modules.facturation.dto.request.UpdateSalesPointRequest;
import com.example.account.modules.facturation.dto.response.SalesPointResponse;
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
@RequestMapping("/api/sales-points")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales Points", description = "Physical/virtual registers sellers operate sessions against")
public class SalesPointController {

    private final SalesPointUseCase salesPointUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a sales point")
    public Mono<SalesPointResponse> create(@Valid @RequestBody CreateSalesPointRequest request) {
        return salesPointUseCase.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a sales point by ID")
    public Mono<SalesPointResponse> getById(@PathVariable UUID id) {
        return salesPointUseCase.findById(id);
    }

    @GetMapping
    @Operation(summary = "List sales points, optionally filtered")
    public Flux<SalesPointResponse> getAll(
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) UUID agencyId) {
        return salesPointUseCase.findAll(organizationId, agencyId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a sales point")
    public Mono<SalesPointResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateSalesPointRequest request) {
        return salesPointUseCase.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a sales point")
    public Mono<Void> delete(@PathVariable UUID id) {
        return salesPointUseCase.delete(id);
    }
}
