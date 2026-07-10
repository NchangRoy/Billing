package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.request.FactureFournisseurCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureFournisseurResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FactureFournisseurServicePort {
    Mono<FactureFournisseurResponse> createFacture(FactureFournisseurCreateRequest request);

    Mono<FactureFournisseurResponse> updateFacture(UUID id, FactureFournisseurCreateRequest request);

    Mono<FactureFournisseurResponse> findById(UUID id);

    Flux<FactureFournisseurResponse> getAllFactures();

    Flux<FactureFournisseurResponse> getByOrganizationId(UUID organizationId);

    Flux<FactureFournisseurResponse> getByAgencyId(UUID agencyId);

    Mono<Void> deleteFacture(UUID id);
}
