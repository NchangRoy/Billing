package com.example.account.modules.core.domain.port.output;

import com.example.account.modules.core.dto.AgencyCreateRequest;
import com.example.account.modules.shared.dto.kernel.KernelAgencyResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AgencyServicePort {
    Flux<KernelAgencyResponse> findAllByOrganization(UUID organizationId);
    Mono<KernelAgencyResponse> findById(UUID organizationId, UUID agencyId);
    Mono<KernelAgencyResponse> create(UUID organizationId, AgencyCreateRequest request);
}
