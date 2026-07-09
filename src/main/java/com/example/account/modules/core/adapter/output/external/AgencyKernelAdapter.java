package com.example.account.modules.core.adapter.output.external;

import com.example.account.modules.core.domain.port.output.AgencyServicePort;
import com.example.account.modules.core.dto.AgencyCreateRequest;
import com.example.account.modules.shared.dto.kernel.KernelAgencyResponse;
import com.example.account.modules.shared.dto.kernel.KernelApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * sales-core has no own agency endpoint — its internal AgencyKernelService calls
 * Kernel directly at GET /api/organizations/{organizationId}/agencies, so Billing
 * does the same here via kernelWebClient instead of going through sales-core.
 */
@Service
@Slf4j
public class AgencyKernelAdapter implements AgencyServicePort {

    private final WebClient kernelWebClient;

    public AgencyKernelAdapter(@Qualifier("kernelWebClient") WebClient kernelWebClient) {
        this.kernelWebClient = kernelWebClient;
    }

    private static final ParameterizedTypeReference<KernelApiResponse<List<KernelAgencyResponse>>> AGENCY_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<KernelApiResponse<KernelAgencyResponse>> AGENCY_TYPE =
            new ParameterizedTypeReference<>() {};

    /**
     * Kernel requires X-Organization-Id even though organizationId is already in the
     * path, so it's set explicitly here rather than relying on the caller's own
     * request context (matches sales-core's AgencyKernelService).
     */
    @Override
    public Flux<KernelAgencyResponse> findAllByOrganization(UUID organizationId) {
        return kernelWebClient
                .get()
                .uri("/api/organizations/{organizationId}/agencies", organizationId)
                .header("X-Organization-Id", organizationId.toString())
                .retrieve()
                .bodyToMono(AGENCY_LIST_TYPE)
                .flatMapMany(resp -> resp == null || resp.getData() == null ? Flux.empty() : Flux.fromIterable(resp.getData()));
    }

    @Override
    public Mono<KernelAgencyResponse> findById(UUID organizationId, UUID agencyId) {
        return findAllByOrganization(organizationId)
                .filter(agency -> agencyId.equals(agency.getId()))
                .next();
    }

    @Override
    public Mono<KernelAgencyResponse> create(UUID organizationId, AgencyCreateRequest request) {
        return kernelWebClient
                .post()
                .uri("/api/organizations/{organizationId}/agencies", organizationId)
                .header("X-Organization-Id", organizationId.toString())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AGENCY_TYPE)
                .map(KernelApiResponse::getData);
    }
}
