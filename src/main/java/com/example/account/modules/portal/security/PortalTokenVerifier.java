package com.example.account.modules.portal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Verifies client-portal JWTs issued by sales-core's ClientPortalJwtService.
 * Both services share the same HMAC secret (client-portal.auth.jwt-secret), so
 * this app can validate the token statelessly without calling sales-core.
 */
@Component
public class PortalTokenVerifier {

    private final SecretKey signingKey;

    public PortalTokenVerifier(@Value("${client-portal.auth.jwt-secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public record PortalPrincipal(UUID accountId, UUID clientId, UUID organizationId, String partyRole, String email) {}

    public PortalPrincipal verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new PortalPrincipal(
                    UUID.fromString(claims.getSubject()),
                    UUID.fromString(claims.get("clientId", String.class)),
                    UUID.fromString(claims.get("organizationId", String.class)),
                    claims.get("partyRole", String.class),
                    claims.get("email", String.class)
            );
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or expired portal token", e);
        }
    }
}
