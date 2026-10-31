package com.example.account.modules.facturation.service;

import com.example.account.modules.facturation.adapter.output.persistence.SellerPersistenceEntity;
import com.example.account.modules.facturation.adapter.output.persistence.SellerR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Authoritative local store for seller profiles — role, permissions, sale
 * point. Replaces what used to be synced to Kernel's sales-core
 * {@code /api/sellers}, which enforced email uniqueness globally across every
 * org on the platform and had no concept of "username" at all beyond a
 * Billing-invented handle that caused real collisions. Uniqueness here is
 * per-organization (changelog-v1.6); login identity is still Kernel's own
 * email+password, matched to this profile by email once Kernel confirms it
 * (see AuthServiceAdapter.enrichWithLocalSeller).
 */
@Service
@RequiredArgsConstructor
public class SellerLocalService {

    private final SellerR2dbcRepository repository;
    private final R2dbcEntityTemplate entityTemplate;

    public Flux<SellerPersistenceEntity> list(UUID organizationId) {
        return repository.findByOrganizationId(organizationId);
    }

    public Flux<SellerPersistenceEntity> findByIds(UUID organizationId, Collection<UUID> ids) {
        return repository.findByOrganizationIdAndIdIn(organizationId, ids);
    }

    public Mono<SellerPersistenceEntity> findByEmail(UUID organizationId, String email) {
        if (email == null || email.isBlank()) {
            return Mono.empty();
        }
        return repository.findByOrganizationIdAndEmailIgnoreCase(organizationId, email);
    }

    public Mono<SellerPersistenceEntity> findById(UUID sellerId) {
        return repository.findById(sellerId);
    }

    public Mono<SellerPersistenceEntity> create(UUID organizationId, String email, String firstName, String lastName,
                                                 String role, List<String> permissions, List<String> permittedSaleSizes,
                                                 String salePoint, UUID salesPointId, String salesPointAddress) {
        LocalDateTime now = LocalDateTime.now();
        SellerPersistenceEntity entity = SellerPersistenceEntity.builder()
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .permissions(toArray(permissions))
                .permittedSaleSizes(toArray(permittedSaleSizes))
                .salePoint(salePoint)
                .salesPointId(salesPointId)
                .salesPointAddress(salesPointAddress)
                .createdAt(now)
                .updatedAt(now)
                .build();
        // Client-generated id means Spring Data would otherwise guess this row
        // already exists and attempt an UPDATE — insert explicitly.
        return entityTemplate.insert(entity);
    }

    public Mono<SellerPersistenceEntity> updateRole(UUID sellerId, String role) {
        return repository.findById(sellerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found: " + sellerId)))
                .flatMap(existing -> {
                    existing.setRole(role);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }

    public Mono<SellerPersistenceEntity> updatePermissions(UUID sellerId, List<String> permissions, List<String> permittedSaleSizes) {
        return repository.findById(sellerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found: " + sellerId)))
                .flatMap(existing -> {
                    existing.setPermissions(toArray(permissions));
                    existing.setPermittedSaleSizes(toArray(permittedSaleSizes));
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }

    private String[] toArray(List<String> values) {
        return values == null ? new String[0] : values.toArray(new String[0]);
    }

    public Mono<SellerPersistenceEntity> updatePhoto(UUID sellerId, String profileImageUrl) {
        return repository.findById(sellerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found: " + sellerId)))
                .flatMap(existing -> {
                    existing.setProfileImageUrl(profileImageUrl);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }

    public Mono<Void> delete(UUID sellerId) {
        return repository.deleteById(sellerId);
    }
}
