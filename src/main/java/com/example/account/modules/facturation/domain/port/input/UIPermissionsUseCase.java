package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.UIPermissionsRequest;
import com.example.account.modules.facturation.dto.response.UIPermissionsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UIPermissionsUseCase {
    Mono<UIPermissionsResponse> create(UIPermissionsRequest request);
    Mono<UIPermissionsResponse> update(UUID id, UIPermissionsRequest request);
    Mono<UIPermissionsResponse> getById(UUID id);
    Flux<UIPermissionsResponse> getBySellerId(UUID sellerId);
    Flux<UIPermissionsResponse> getByOrganizationId(UUID organizationId);
    Flux<UIPermissionsResponse> getByAgencyId(UUID agencyId);
    Mono<Void> delete(UUID id);
}
