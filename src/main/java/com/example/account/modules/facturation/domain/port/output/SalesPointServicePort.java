package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.request.CreateSalesPointRequest;
import com.example.account.modules.facturation.dto.request.UpdateSalesPointRequest;
import com.example.account.modules.facturation.dto.response.SalesPointResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SalesPointServicePort {
    Mono<SalesPointResponse> create(CreateSalesPointRequest request);
    Mono<SalesPointResponse> findById(UUID id);
    Flux<SalesPointResponse> findAll(UUID organizationId, UUID agencyId);
    Mono<SalesPointResponse> update(UUID id, UpdateSalesPointRequest request);
    Mono<Void> delete(UUID id);
}
