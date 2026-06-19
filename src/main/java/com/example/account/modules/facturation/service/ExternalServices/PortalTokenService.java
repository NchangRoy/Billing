package com.example.account.modules.facturation.service.ExternalServices;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.account.modules.facturation.dto.response.ExternalResponses.AccessPortalPermissionsDTO;
import com.example.account.modules.facturation.model.entity.Others.PortalAccessToken;
import com.example.account.modules.facturation.repository.Others.PortalAccessTokenRepository;
import com.example.account.modules.facturation.service.ExternalServices.entity.PortalPermissions;
import com.example.account.modules.facturation.service.ExternalServices.entity.enums.ResourceType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortalTokenService {

    private final PortalAccessTokenRepository repository;

    /**
     * Creates a new access token for a specific resource.
     */
    public Mono<PortalAccessToken> createToken(
    UUID resourceId, 
    ResourceType resourceType,  
    String clientEmail,
    PortalPermissions permissions
    
) {
    PortalPermissions actualPermissions = permissions != null ? permissions : PortalPermissions.builder()
            .canView(true)
            .canAccept(true)
            .canModify(false)
            .canReject(true)
            .build();
    PortalAccessToken newToken = PortalAccessToken.builder()
            .resourceId(resourceId)
            .resourceType(resourceType)
            .clientEmail(clientEmail)
            .canAccept(actualPermissions.getCanAccept())
            .canView(actualPermissions.getCanView())
            .canModify(actualPermissions.getCanModify())
            .canReject(actualPermissions.getCanReject())
            .token(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""))
            .used(false)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(7)) // Default 7 days expiry
            .build();

    return repository.save(newToken);
}

    /**
     * Retrieves all tokens in the system.
     */
    public Flux<PortalAccessToken> getAllTokens() {
        return repository.findAll();
    }

    /**
     * Finds a specific token for validation.
     */
    public Mono<PortalAccessToken> getByTokenValue(String tokenValue) {
        return repository.findByToken(tokenValue);
    }
   public Mono<PortalAccessToken> markAsUsed(String tokenValue) {
    return repository.findByToken(tokenValue)
        .switchIfEmpty(Mono.error(new RuntimeException("Token not found")))
        .flatMap(token -> {
            token.setUsed(true);
            // Return the result of the save operation
            return repository.save(token); 
        });
}
}