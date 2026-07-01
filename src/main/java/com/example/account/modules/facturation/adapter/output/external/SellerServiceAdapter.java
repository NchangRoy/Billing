package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.facturation.domain.port.output.SellerServicePort;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import com.example.account.modules.shared.dto.kernel.KernelCashierResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class SellerServiceAdapter implements SellerServicePort {

    private final WebClient kernelWebClient;

    public SellerServiceAdapter(@Qualifier("kernelWebClient") WebClient kernelWebClient) {
        this.kernelWebClient = kernelWebClient;
    }

    @Override
    public Flux<SellerAuthResponse> getSellersByOrganization(UUID organizationId) {
        log.info("Fetching cashiers from Kernel for organization: {}", organizationId);
        // /api/cashiers returns a plain JSON array, not a {success, data} wrapper
        return kernelWebClient
                .get()
                .uri(u -> u.path("/api/cashiers").queryParam("organizationId", organizationId).build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), resp -> {
                    log.error("Kernel cashiers error: {}", resp.statusCode());
                    return Mono.empty();
                })
                .bodyToFlux(KernelCashierResponse.class)
                .map(this::mapToSellerAuthResponse)
                .doOnComplete(() -> log.info("Fetched cashiers for organization: {}", organizationId))
                .onErrorResume(e -> {
                    log.error("Error fetching cashiers: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    private SellerAuthResponse mapToSellerAuthResponse(KernelCashierResponse cashier) {
        SellerAuthResponse r = new SellerAuthResponse();
        r.setId(cashier.getId());
        r.setUsername(cashier.getFullName());
        r.setOrganizationId(cashier.getOrganizationId());
        r.setAgencyId(cashier.getAgencyId());
        r.setCreatedAt(cashier.getCreatedAt());
        return r;
    }
}
