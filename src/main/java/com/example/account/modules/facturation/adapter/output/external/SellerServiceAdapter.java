package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.core.exception.SalesCoreErrorMapper;
import com.example.account.modules.facturation.domain.port.output.SellerServicePort;
import com.example.account.modules.facturation.dto.request.AssignAgencyRequest;
import com.example.account.modules.facturation.dto.request.CreateSellerRequest;
import com.example.account.modules.facturation.dto.request.SellerUIPermissionsRequest;
import com.example.account.modules.facturation.dto.request.UpdateSellerPermissionsRequest;
import com.example.account.modules.facturation.dto.request.UpdateSellerPhotoRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.AssignAgencyResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.CreateSellerResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerListItemResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerUIPermissionsResponse;
import com.example.account.modules.shared.dto.kernel.KernelCashierResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Function;

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

    @Override
    public Flux<SellerListItemResponse> listSellers(UUID organizationId) {
        return salesCoreWebClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/sellers/local").queryParam("organizationId", organizationId).build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(SellerListItemResponse.class);
    }

    private static final Function<org.springframework.web.reactive.function.client.ClientResponse, Mono<? extends Throwable>> SALES_CORE_ERROR =
            SalesCoreErrorMapper.forContext("sales-core sellers error");

    @Override
    public Mono<CreateSellerResponse> createSeller(CreateSellerRequest request) {
        return salesCoreWebClient
                .post()
                .uri("/api/sellers")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(CreateSellerResponse.class);
    }

    @Override
    public Mono<AssignAgencyResponse> assignAgency(UUID sellerId, AssignAgencyRequest request) {
        return salesCoreWebClient
                .post()
                .uri("/api/sellers/{sellerId}/agency", sellerId)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(AssignAgencyResponse.class);
    }

    @Override
    public Mono<SellerUIPermissionsResponse> getUIPermissions(UUID sellerId) {
        return salesCoreWebClient
                .get()
                .uri("/api/sellers/{sellerId}/ui-permissions", sellerId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SellerUIPermissionsResponse.class);
    }

    @Override
    public Mono<SellerUIPermissionsResponse> setUIPermissions(UUID sellerId, SellerUIPermissionsRequest request) {
        return salesCoreWebClient
                .post()
                .uri("/api/sellers/{sellerId}/ui-permissions", sellerId)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SellerUIPermissionsResponse.class);
    }

    @Override
    public Mono<SellerListItemResponse> updatePermissions(UUID sellerId, UpdateSellerPermissionsRequest request) {
        return salesCoreWebClient
                .put()
                .uri("/api/sellers/{sellerId}/permissions", sellerId)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SellerListItemResponse.class);
    }

    @Override
    public Mono<SellerListItemResponse> updatePhoto(UUID sellerId, UpdateSellerPhotoRequest request) {
        return salesCoreWebClient
                .put()
                .uri("/api/sellers/{sellerId}/photo", sellerId)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SellerListItemResponse.class);
    }

    @Override
    public Mono<Void> deleteSeller(UUID sellerId) {
        return salesCoreWebClient
                .delete()
                .uri("/api/sellers/{sellerId}", sellerId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(Void.class);
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
