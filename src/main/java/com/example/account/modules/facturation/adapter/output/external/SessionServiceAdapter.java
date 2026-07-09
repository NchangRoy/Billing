package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.core.exception.SalesCoreErrorMapper;
import com.example.account.modules.facturation.domain.port.output.SessionServicePort;
import com.example.account.modules.facturation.dto.request.CloseSessionRequest;
import com.example.account.modules.facturation.dto.request.CreateSessionRequest;
import com.example.account.modules.facturation.dto.request.UpdateSessionRequest;
import com.example.account.modules.facturation.dto.response.SessionResponse;
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
public class SessionServiceAdapter implements SessionServicePort {

    private final WebClient salesCoreWebClient;

    public SessionServiceAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient) {
        this.salesCoreWebClient = salesCoreWebClient;
    }

    private static final Function<org.springframework.web.reactive.function.client.ClientResponse, Mono<? extends Throwable>> SALES_CORE_ERROR =
            SalesCoreErrorMapper.forContext("sales-core sessions error");

    @Override
    public Mono<SessionResponse> open(CreateSessionRequest request) {
        return salesCoreWebClient
                .post()
                .uri("/api/sessions")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SessionResponse.class);
    }

    @Override
    public Mono<SessionResponse> schedule(CreateSessionRequest request) {
        return salesCoreWebClient
                .post()
                .uri("/api/sessions/schedule")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SessionResponse.class);
    }

    @Override
    public Mono<SessionResponse> start(UUID id) {
        return salesCoreWebClient
                .post()
                .uri("/api/sessions/{id}/start", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SessionResponse.class);
    }

    @Override
    public Mono<SessionResponse> close(UUID id, CloseSessionRequest request) {
        return salesCoreWebClient
                .post()
                .uri("/api/sessions/{id}/close", id)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SessionResponse.class);
    }

    @Override
    public Mono<SessionResponse> suspend(UUID id) {
        return salesCoreWebClient
                .post()
                .uri("/api/sessions/{id}/suspend", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SessionResponse.class);
    }

    @Override
    public Mono<SessionResponse> resume(UUID id) {
        return salesCoreWebClient
                .post()
                .uri("/api/sessions/{id}/resume", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SessionResponse.class);
    }

    @Override
    public Mono<SessionResponse> cancel(UUID id) {
        return salesCoreWebClient
                .post()
                .uri("/api/sessions/{id}/cancel", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SessionResponse.class);
    }

    @Override
    public Mono<SessionResponse> reopen(UUID id) {
        return salesCoreWebClient
                .post()
                .uri("/api/sessions/{id}/reopen", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SessionResponse.class);
    }

    @Override
    public Mono<SessionResponse> findById(UUID id) {
        return salesCoreWebClient
                .get()
                .uri("/api/sessions/{id}", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SessionResponse.class);
    }

    @Override
    public Flux<SessionResponse> findAll(UUID salesPointId, UUID sellerId, UUID organizationId, UUID agencyId) {
        return salesCoreWebClient
                .get()
                .uri(builder -> builder.path("/api/sessions")
                        .queryParamIfPresent("salesPointId", java.util.Optional.ofNullable(salesPointId))
                        .queryParamIfPresent("sellerId", java.util.Optional.ofNullable(sellerId))
                        .queryParamIfPresent("organizationId", java.util.Optional.ofNullable(organizationId))
                        .queryParamIfPresent("agencyId", java.util.Optional.ofNullable(agencyId))
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(SessionResponse.class);
    }

    @Override
    public Mono<SessionResponse> update(UUID id, UpdateSessionRequest request) {
        return salesCoreWebClient
                .put()
                .uri("/api/sessions/{id}", id)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SessionResponse.class);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return salesCoreWebClient
                .delete()
                .uri("/api/sessions/{id}", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(Void.class);
    }
}
