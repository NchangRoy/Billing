package com.example.account.modules.core.filter;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * WebFilter responsible for extracting the Organization ID from the request header 
 * and setting it in the Reactor Context.
 */
@Component
@Order(-100) // Execute early in the filter chain
@Slf4j
public class OrganizationFilter implements WebFilter {

    public static final String ORGANIZATION_ID_HEADER = "X-Organization-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String orgIdHeader = exchange.getRequest().getHeaders().getFirst(ORGANIZATION_ID_HEADER);
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        return chain.filter(exchange)
                .contextWrite(ctx -> {
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        ctx = ctx.put(ReactiveOrganizationContext.TOKEN_KEY, authHeader.substring(7));
                    }
                    if (orgIdHeader != null && !orgIdHeader.isBlank()) {
                        try {
                            UUID orgId = UUID.fromString(orgIdHeader);
                            log.debug("Found Organization ID in header: {}", orgId);
                            ctx = ctx.put(ReactiveOrganizationContext.ORGANIZATION_ID_KEY, orgId);
                        } catch (IllegalArgumentException e) {
                            log.error("Invalid Organization ID format in header: {}", orgIdHeader);
                        }
                    }
                    return ctx;
                });
    }
}
