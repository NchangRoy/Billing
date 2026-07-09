package com.example.account.modules.facturation.adapter.output.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * AccountingServiceAdapter posts to a separate accounting backend and needs a
 * real Kernel-issued JWT to do so. Since seller login now goes through
 * sales-core's own local auth (a different token type entirely — see
 * SalesCoreWebClientConfig), this service authenticates to Kernel independently
 * of whatever the caller's own token is, so accounting sync keeps working
 * regardless of how the seller logged in. Uses its own WebClient (not the
 * shared kernelWebClient bean) to avoid looping back through that bean's own
 * bearer-injection filter.
 */
@Service
@Slf4j
public class AccountingKernelAuthService {

    private final WebClient authWebClient;
    private final String username;
    private final String password;
    private final Mono<AuthData> cachedAuth;

    public AccountingKernelAuthService(
            WebClient.Builder builder,
            @Value("${comops.kernel.base-url}") String baseUrl,
            @Value("${comops.kernel.client-id}") String clientId,
            @Value("${comops.kernel.api-key}") String apiKey,
            @Value("${comops.kernel.tenant-id}") String tenantId,
            @Value("${comops.kernel.service-account.username}") String username,
            @Value("${comops.kernel.service-account.password}") String password) {
        this.authWebClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("X-Client-Id", clientId)
                .defaultHeader("X-Api-Key", apiKey)
                .defaultHeader("X-Tenant-Id", tenantId)
                .build();
        this.username = username;
        this.password = password;
        this.cachedAuth = Mono.defer(this::login)
                .cache(this::ttlFor, error -> Duration.ZERO, () -> Duration.ZERO);
    }

    public Mono<String> getValidToken() {
        return cachedAuth.map(AuthData::getAccessToken);
    }

    private Mono<AuthData> login() {
        log.info("Logging in to Kernel as accounting service account '{}'", username);
        return authWebClient.post()
                .uri("/api/auth/login")
                .bodyValue(Map.of("principal", username, "password", password))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<AuthData>>() {})
                .map(ApiResponse::getData)
                .doOnNext(auth -> log.info("Kernel accounting service-account login succeeded, token expires in {}s",
                        auth.getExpiresInSeconds()));
    }

    private Duration ttlFor(AuthData auth) {
        long expiresIn = auth.getExpiresInSeconds() != null ? auth.getExpiresInSeconds() : 60;
        return Duration.ofSeconds(Math.max(expiresIn - 60, 30));
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiResponse<T> {
        private Boolean success;
        private T data;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AuthData {
        private String accessToken;
        private Long expiresInSeconds;
    }
}
