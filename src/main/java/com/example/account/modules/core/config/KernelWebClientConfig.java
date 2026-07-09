package com.example.account.modules.core.config;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.facturation.adapter.output.external.AccountingKernelAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
@Slf4j
public class KernelWebClientConfig {

    @Value("${comops.kernel.base-url}")
    private String baseUrl;

    @Value("${comops.kernel.client-id}")
    private String clientId;

    @Value("${comops.kernel.api-key}")
    private String apiKey;

    @Value("${comops.kernel.tenant-id}")
    private String tenantId;

    @Bean
    @Qualifier("kernelWebClient")
    public WebClient kernelWebClient(WebClient.Builder builder, AccountingKernelAuthService authService) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader("X-Client-Id", clientId)
                .defaultHeader("X-Api-Key", apiKey)
                .defaultHeader("X-Tenant-Id", tenantId)
                .filter(injectBearerToken(authService))
                .filter(injectOrganizationId())
                .build();
    }

    /**
     * Sellers authenticate locally against sales-core, not Kernel, so their access
     * token is never a valid Kernel token. Every kernelWebClient call instead
     * authenticates as the platform's Kernel service account (same cached-token
     * pattern as AccountingKernelAuthService) rather than forwarding the caller's
     * own token.
     */
    private ExchangeFilterFunction injectBearerToken(AccountingKernelAuthService authService) {
        return (request, next) -> {
            if (request.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
                return next.exchange(request);
            }
            return authService.getValidToken()
                    .flatMap(token -> next.exchange(ClientRequest.from(request)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .build()));
        };
    }

    /**
     * Reads the organization ID from the Reactor context and adds X-Organization-Id to all Kernel requests.
     */
    private ExchangeFilterFunction injectOrganizationId() {
        return (request, next) -> Mono.deferContextual(ctx -> {
            UUID orgId = ctx.getOrDefault(ReactiveOrganizationContext.ORGANIZATION_ID_KEY, null);
            if (orgId != null) {
                ClientRequest modified = ClientRequest.from(request)
                        .header("X-Organization-Id", orgId.toString())
                        .build();
                return next.exchange(modified);
            }
            return next.exchange(request);
        });
    }
}
