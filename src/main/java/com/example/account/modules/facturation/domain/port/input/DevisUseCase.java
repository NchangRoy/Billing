package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.DevisCreateRequest;
import com.example.account.modules.facturation.dto.response.DevisResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import com.example.account.modules.facturation.model.enums.StatutDevis;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

import com.example.account.modules.facturation.dto.request.ExternalRequest.EmailRequest;

public interface DevisUseCase {
    Mono<DevisResponse> createDevis(DevisCreateRequest request);
    Mono<DevisResponse> updateDevis(UUID devisId, DevisCreateRequest request);
    Mono<DevisResponse> getDevisById(UUID devisId);
    Mono<DevisResponse> getDevisByNumero(String numeroDevis);
    Flux<DevisResponse> getAllDevis();
    Flux<DevisResponse> getAllDevis(Pageable pageable);
    Mono<Void> deleteDevis(UUID devisId);
    Mono<Void> accepterDevis(UUID devisId);
    Mono<Void> refuserDevis(UUID devisId);
    Mono<Void> sendDevisAsEmail(EmailRequest emailRequest);
    Mono<Void> sendToPortal(UUID devisId);
    Flux<SellerAuthResponse> enrichDevis(UUID orgId);
    Flux<DevisResponse> getDevisByOrganizationId(UUID organizationId);
    Flux<DevisResponse> getDevisByAgencyId(UUID agencyId);
    Flux<DevisResponse> getDevisBySellerId(UUID sellerId);
}
