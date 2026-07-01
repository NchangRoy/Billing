package com.example.account.modules.core.context;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.util.UUID;

/**
 * Reactive context storage for the current organization context.
 * Utilizes the Reactor Context to propagate organization and user information
 * across different threads and non-blocking operations.
 */
@Slf4j
public class ReactiveOrganizationContext {

    public static final String ORGANIZATION_ID_KEY = "organizationId";
    public static final String USER_ID_KEY = "userId";
    public static final String TOKEN_KEY = "bearerToken";

    private ReactiveOrganizationContext() {
        // Private constructor
    }

    /**
     * Gets the current organization ID from the Reactor Context.
     */
    public static Mono<UUID> getOrganizationId() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(ORGANIZATION_ID_KEY)) {
                return Mono.just(ctx.get(ORGANIZATION_ID_KEY));
            }
            return Mono.error(new IllegalStateException("No organization context set in Reactor Context"));
        });
    }

    /**
     * Gets the current organization ID from the Reactor Context without throwing error.
     */
    public static Mono<UUID> getOrganizationIdOrEmpty() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(ORGANIZATION_ID_KEY)) {
                return Mono.just(ctx.get(ORGANIZATION_ID_KEY));
            }
            return Mono.empty();
        });
    }

    /**
     * Gets the current user ID from the Reactor Context.
     */
    public static Mono<UUID> getUserId() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(USER_ID_KEY)) {
                return Mono.just(ctx.get(USER_ID_KEY));
            }
            return Mono.empty(); // User ID might not always be present
        });
    }

    /**
     * Gets the Bearer token from the Reactor Context (without "Bearer " prefix).
     */
    public static Mono<String> getBearerToken() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(TOKEN_KEY)) {
                return Mono.just(ctx.get(TOKEN_KEY));
            }
            return Mono.empty();
        });
    }

    /**
     * Utility to check if context exists.
     */
    public static Mono<Boolean> hasOrganizationContext() {
        return Mono.deferContextual(ctx -> Mono.just(ctx.hasKey(ORGANIZATION_ID_KEY)));
    }
}
