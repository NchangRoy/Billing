package com.example.account.modules.core.config;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
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

/**
 * Products/customers/fournisseurs/sellers/auth now live in sales-core, not
 * Kernel directly (sales-core itself talks to Kernel and adds its own
 * augmentation on top, e.g. allowedSaleSizes/ntva for third parties). Forwards
 * the caller's own Authorization + X-Organization-Id, same as kernelWebClient —
 * sales-core's Kernel leg is hardwired to a service account internally, but
 * org-scoping on sales-core's own endpoints still depends on these headers.
 */
@Configuration
@Slf4j
public class SalesCoreWebClientConfig {

    @Value("${sales-core.base-url}")
    private String baseUrl;

    @Bean
    @Qualifier("salesCoreWebClient")
    public WebClient salesCoreWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .filter(injectBearerToken())
                .filter(injectOrganizationId())
                .build();
    }

    private ExchangeFilterFunction injectBearerToken() {
        return (request, next) -> Mono.deferContextual(ctx -> {
            String token = ctx.getOrDefault(ReactiveOrganizationContext.TOKEN_KEY, null);
            if (token != null && !request.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
                ClientRequest modified = ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build();
                return next.exchange(modified);
            }
            return next.exchange(request);
        });
    }

    private ExchangeFilterFunction injectOrganizationId() {
        return (request, next) -> {
            if (request.headers().containsKey("X-Organization-Id")) {
                return next.exchange(request);
            }
            return Mono.deferContextual(ctx -> {
                UUID orgId = ctx.getOrDefault(ReactiveOrganizationContext.ORGANIZATION_ID_KEY, null);
                if (orgId != null) {
                    ClientRequest modified = ClientRequest.from(request)
                            .header("X-Organization-Id", orgId.toString())
                            .build();
                    return next.exchange(modified);
                }
                return next.exchange(request);
            });
        };
    }
}
