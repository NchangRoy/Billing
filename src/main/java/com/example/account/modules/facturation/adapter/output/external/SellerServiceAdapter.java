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

    private final WebClient salesCoreWebClient;

    public SellerServiceAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient) {
        this.salesCoreWebClient = salesCoreWebClient;
    }

    @Override
    public Flux<SellerAuthResponse> getSellersByOrganization(UUID organizationId) {
        log.info("Fetching cashiers from sales-core for organization: {}", organizationId);
        // sales-core's /api/sellers derives the org from X-Organization-Id, not a query
        // param, so it's set explicitly here to honor the org this method was called with.
        return salesCoreWebClient
                .get()
                .uri("/api/sellers")
                .header("X-Organization-Id", organizationId.toString())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), resp -> {
                    log.error("sales-core sellers error: {}", resp.statusCode());
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
