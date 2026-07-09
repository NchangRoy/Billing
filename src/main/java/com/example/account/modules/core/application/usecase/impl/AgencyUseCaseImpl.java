package com.example.account.modules.core.application.usecase.impl;

import com.example.account.modules.core.domain.port.input.AgencyUseCase;
import com.example.account.modules.core.domain.port.output.AgencyServicePort;
import com.example.account.modules.core.dto.AgencyCreateRequest;
import com.example.account.modules.shared.dto.kernel.KernelAgencyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgencyUseCaseImpl implements AgencyUseCase {

    private final AgencyServicePort agencyServicePort;

    @Override
    public Flux<KernelAgencyResponse> findAllByOrganization(UUID organizationId) {
        return agencyServicePort.findAllByOrganization(organizationId);
    }

    @Override
    public Mono<KernelAgencyResponse> findById(UUID organizationId, UUID agencyId) {
        return agencyServicePort.findById(organizationId, agencyId);
    }

    @Override
    public Mono<KernelAgencyResponse> create(UUID organizationId, AgencyCreateRequest request) {
        return agencyServicePort.create(organizationId, request);
    }
}
