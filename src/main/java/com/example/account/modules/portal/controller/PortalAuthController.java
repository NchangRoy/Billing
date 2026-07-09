package com.example.account.modules.portal.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Thin proxy so the frontend only ever talks to this app's base URL. The
 * client-portal auth realm actually lives in sales-core (its own JWT secret,
 * separate from seller/Kernel auth) — this just forwards the call there.
 */
@RestController
@RequestMapping("/api/portal/auth")
public class PortalAuthController {

    private final WebClient salesCoreWebClient;

    public PortalAuthController(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient) {
        this.salesCoreWebClient = salesCoreWebClient;
    }

    @PostMapping("/login")
    public Mono<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        return salesCoreWebClient
                .post()
                .uri("/api/client-portal/auth/login")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    @PostMapping("/change-password")
    public Mono<Map<String, Object>> changePassword(@RequestBody Map<String, String> request) {
        return salesCoreWebClient
                .post()
                .uri("/api/client-portal/auth/change-password")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
