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

public interface DevisUseCase {
    Mono<DevisResponse> createDevis(DevisCreateRequest request);
    Mono<DevisResponse> updateDevis(UUID devisId, DevisCreateRequest request);
    Mono<DevisResponse> getDevisById(UUID devisId);
    Mono<DevisResponse> getDevisByNumero(String numeroDevis);
    Flux<DevisResponse> getAllDevis();
    Flux<DevisResponse> getAllDevis(Pageable pageable);
    Flux<DevisResponse> getDevisByClient(UUID clientId);
    Flux<DevisResponse> getDevisByStatut(StatutDevis statut);
    Flux<DevisResponse> getDevisExpires();
    Flux<DevisResponse> getDevisByPeriode(LocalDate dateDebut, LocalDate dateFin);
    Mono<Void> deleteDevis(UUID devisId);
    Mono<DevisResponse> accepterDevis(UUID devisId);
    Mono<DevisResponse> refuserDevis(UUID devisId, String motifRefus);
    Flux<SellerAuthResponse> enrichDevis(UUID orgId);
}
