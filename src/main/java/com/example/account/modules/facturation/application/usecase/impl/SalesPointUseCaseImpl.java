package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.port.input.SalesPointUseCase;
import com.example.account.modules.facturation.domain.port.output.SalesPointServicePort;
import com.example.account.modules.facturation.dto.request.CreateSalesPointRequest;
import com.example.account.modules.facturation.dto.request.UpdateSalesPointRequest;
import com.example.account.modules.facturation.dto.response.SalesPointResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesPointUseCaseImpl implements SalesPointUseCase {

    private final SalesPointServicePort salesPointServicePort;

    @Override
    public Mono<SalesPointResponse> create(CreateSalesPointRequest request) {
        return salesPointServicePort.create(request);
    }

    @Override
    public Mono<SalesPointResponse> findById(UUID id) {
        return salesPointServicePort.findById(id);
    }

    @Override
    public Flux<SalesPointResponse> findAll(UUID organizationId, UUID agencyId) {
        return salesPointServicePort.findAll(organizationId, agencyId);
    }

    @Override
    public Mono<SalesPointResponse> update(UUID id, UpdateSalesPointRequest request) {
        return salesPointServicePort.update(id, request);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return salesPointServicePort.delete(id);
    }
}
