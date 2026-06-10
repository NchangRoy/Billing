package com.example.account.modules.facturation.repository.Others;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.example.account.modules.facturation.model.entity.Others.PortalAccessToken;

import reactor.core.publisher.Mono;
import java.util.List;


@Repository
public interface PortalAccessTokenRepository extends ReactiveCrudRepository<PortalAccessToken, Long> {
    
    // Finds the token and ensures it's still valid/unused
    Mono<PortalAccessToken> findByToken(String token);
    
    // Security check: Find by token and email
    Mono<PortalAccessToken> findByTokenAndClientEmail(String token, String clientEmail);
}