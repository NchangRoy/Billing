package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.ProformaInvoiceRequest;
import com.example.account.modules.facturation.dto.response.ProformaInvoiceResponse;
import com.example.account.modules.facturation.model.enums.StatutProforma;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FactureProformaUseCase {
    Mono<ProformaInvoiceResponse> createProforma(ProformaInvoiceRequest request);
    Mono<ProformaInvoiceResponse> getProformaById(UUID id);
    Flux<ProformaInvoiceResponse> getAllProformas();
    Flux<ProformaInvoiceResponse> getProformasByClient(UUID idClient);
    Mono<Void> deleteProforma(UUID id);
    Mono<ProformaInvoiceResponse> updateStatut(UUID id, StatutProforma nouveauStatut);
    Mono<ProformaInvoiceResponse> updateFactureProforma(UUID id, ProformaInvoiceRequest request);
    Flux<ProformaInvoiceResponse> getProformasByOrganizationId(UUID organizationId);
    Flux<ProformaInvoiceResponse> getProformasByAgencyId(UUID agencyId);
}
