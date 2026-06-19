package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.FactureCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import com.example.account.modules.facturation.model.enums.StatutFacture;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface FactureUseCase {
    Mono<FactureResponse> createFacture(FactureCreateRequest request);
    Mono<FactureResponse> updateFacture(UUID factureId, FactureCreateRequest request);
    Mono<FactureResponse> getFactureById(UUID factureId);
    Mono<Void> accountFacture(UUID factureId);
    Mono<FactureResponse> getFactureByNumero(String numeroFacture);
    Flux<FactureResponse> getAllFactures();
    Flux<FactureResponse> getAllFactures(Pageable pageable);
    Flux<FactureResponse> getFacturesByClient(UUID clientId);
    Flux<FactureResponse> getFacturesByEtat(StatutFacture etat);
    Flux<FactureResponse> getFacturesEnRetard();
    Flux<FactureResponse> getFacturesNonPayees();
    Flux<FactureResponse> getFacturesByPeriode(LocalDate dateDebut, LocalDate dateFin);
    Mono<Void> deleteFacture(UUID factureId);
    Mono<FactureResponse> marquerCommePaye(UUID factureId);
    Mono<FactureResponse> enregistrerPaiement(UUID factureId, BigDecimal montantPaye);
    Mono<Long> countByEtat(StatutFacture etat);
    Flux<FactureResponse> getFacturesByOrganizationId(UUID organizationId);
    Flux<FactureResponse> getFacturesByAgencyId(UUID agencyId);
}
