package com.example.account.modules.facturation.service.ExternalServices;


import com.example.account.modules.facturation.dto.response.ExternalResponses.AccessPortalPermissionsDTO;
import com.example.account.modules.facturation.model.entity.Others.PortalAccessToken;
import com.example.account.modules.facturation.service.ExternalServices.entity.enums.ResourceType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portal-tokens")
@RequiredArgsConstructor
public class PortalTokenController {

    private final PortalTokenService tokenService;

    /**
     * 1. CREATE TOKEN (Internal API)
     * Used by your system when sending an email to a client.
     */
    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PortalAccessToken> generateToken(
            @RequestParam UUID resourceId,
            @RequestParam ResourceType resourceType,
            @RequestParam String clientEmail
          
    ) {
        return tokenService.createToken(resourceId, resourceType, clientEmail,null);
    }

    /**
     * 2. VALIDATE TOKEN (Public API)
     * Used by your Next.js 'use client' page to fetch data on load.
     */
    @GetMapping("/validate/{token}")
    public Mono<ResponseEntity<PortalAccessToken>> validateAndGet(
            @PathVariable String token
    ) {
        return tokenService.getByTokenValue(token)
            // Ensure token isn't used and hasn't expired
            .filter(t -> !t.isUsed()) 
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.GONE).build());
    }

    /**
     * 3. GET ALL TOKENS (Admin API)
     * For monitoring link activity.
     */
    @GetMapping
    public Flux<PortalAccessToken> getAll() {
        return tokenService.getAllTokens();
    }
}