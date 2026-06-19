package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.domain.model.UIPermissions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UIPermissionsRepositoryPort {
    Mono<UIPermissions> insert(UIPermissions uiPermissions);
    Mono<UIPermissions> save(UIPermissions uiPermissions);
    Mono<UIPermissions> findById(UUID id);
    Flux<UIPermissions> findBySellerId(UUID sellerId);
    Flux<UIPermissions> findByOrganizationId(UUID organizationId);
    Flux<UIPermissions> findByAgencyId(UUID agencyId);
    Mono<Void> deleteById(UUID id);
}
