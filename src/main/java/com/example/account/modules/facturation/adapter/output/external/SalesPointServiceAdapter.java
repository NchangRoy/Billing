package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.core.exception.SalesCoreErrorMapper;
import com.example.account.modules.facturation.domain.port.output.SalesPointServicePort;
import com.example.account.modules.facturation.dto.request.CreateSalesPointRequest;
import com.example.account.modules.facturation.dto.request.UpdateSalesPointRequest;
import com.example.account.modules.facturation.dto.response.SalesPointResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class SalesPointServiceAdapter implements SalesPointServicePort {

    private final WebClient salesCoreWebClient;

    public SalesPointServiceAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient) {
        this.salesCoreWebClient = salesCoreWebClient;
    }

    private static final Function<org.springframework.web.reactive.function.client.ClientResponse, Mono<? extends Throwable>> SALES_CORE_ERROR =
            SalesCoreErrorMapper.forContext("sales-core sales-points error");

    @Override
    public Mono<SalesPointResponse> create(CreateSalesPointRequest request) {
        return salesCoreWebClient
                .post()
                .uri("/api/sales-points")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SalesPointResponse.class);
    }

    @Override
    public Mono<SalesPointResponse> findById(UUID id) {
        return salesCoreWebClient
                .get()
                .uri("/api/sales-points/{id}", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SalesPointResponse.class);
    }

    @Override
    public Flux<SalesPointResponse> findAll(UUID organizationId, UUID agencyId) {
        return salesCoreWebClient
                .get()
                .uri(builder -> builder.path("/api/sales-points")
                        .queryParamIfPresent("organizationId", Optional.ofNullable(organizationId))
                        .queryParamIfPresent("agencyId", Optional.ofNullable(agencyId))
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(SalesPointResponse.class);
    }

    @Override
    public Mono<SalesPointResponse> update(UUID id, UpdateSalesPointRequest request) {
        return salesCoreWebClient
                .put()
                .uri("/api/sales-points/{id}", id)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SalesPointResponse.class);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return salesCoreWebClient
                .delete()
                .uri("/api/sales-points/{id}", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(Void.class);
    }
}
