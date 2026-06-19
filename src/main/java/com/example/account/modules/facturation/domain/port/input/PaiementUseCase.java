package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.PaiementCreateRequest;
import com.example.account.modules.facturation.dto.request.PaiementUpdateRequest;
import com.example.account.modules.facturation.dto.response.PaiementResponse;
import com.example.account.modules.facturation.model.enums.TypePaiement;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface PaiementUseCase {
    Mono<PaiementResponse> createPaiement(PaiementCreateRequest request);
    Mono<PaiementResponse> updatePaiement(UUID paiementId, PaiementUpdateRequest request);
    Mono<PaiementResponse> getPaiementById(UUID paiementId);
    Flux<PaiementResponse> getAllPaiements();
    Flux<PaiementResponse> getAllPaiements(Pageable pageable);
    Flux<PaiementResponse> getPaiementsByClient(UUID clientId);
    Flux<PaiementResponse> getPaiementsByFacture(UUID factureId);
    Flux<PaiementResponse> getPaiementsByModePaiement(TypePaiement modePaiement);
    Flux<PaiementResponse> getPaiementsByPeriode(LocalDate dateDebut, LocalDate dateFin);
    Mono<Void> deletePaiement(UUID paiementId);
    Mono<BigDecimal> getTotalPaiementsByClient(UUID clientId);
    Mono<BigDecimal> getTotalPaiementsByFacture(UUID factureId);
    Mono<BigDecimal> getTotalPaiementsByPeriode(LocalDate dateDebut, LocalDate dateFin);
    Mono<Long> countPaiementsByClient(UUID clientId);
    Mono<Long> countPaiementsByModePaiement(TypePaiement modePaiement);
    Flux<PaiementResponse> getPaiementsByOrganizationId(UUID organizationId);
    Flux<PaiementResponse> getPaiementsByAgencyId(UUID agencyId);
}
