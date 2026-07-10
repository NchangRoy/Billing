package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.request.FactureCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureResponse;
import com.example.account.modules.facturation.model.enums.StatutFacture;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Port for delegating Facture (client invoice) persistence operations to sales-core.
 * account has no local Facture table after this migration; all CRUD goes through this port.
 */
public interface FactureServicePort {

    Mono<Map<String, Object>> getAccountingSaleFacture(UUID factureId);

    Mono<Map<String, Object>> getAccountingPurchaseFacture(UUID factureId);

    Mono<FactureResponse> createFacture(FactureCreateRequest request);

    Mono<FactureResponse> updateFacture(UUID factureId, FactureCreateRequest request);

    Mono<FactureResponse> findById(UUID factureId);

    Mono<FactureResponse> findByNumeroFacture(String numeroFacture);

    Flux<FactureResponse> getAllFactures();

    Flux<FactureResponse> getAllFactures(Pageable pageable);

    Flux<FactureResponse> findByIdClient(UUID clientId);

    Flux<FactureResponse> findByEtat(StatutFacture etat);

    Flux<FactureResponse> findOverdueFactures();

    Flux<FactureResponse> findUnpaidFactures();

    Flux<FactureResponse> findByDateFacturationBetween(LocalDate dateDebut, LocalDate dateFin);

    Mono<Void> deleteFacture(UUID factureId);

    Mono<FactureResponse> marquerCommePaye(UUID factureId);

    Mono<FactureResponse> enregistrerPaiement(UUID factureId, BigDecimal montantPaye);

    Mono<Long> countByEtat(StatutFacture etat);

    Flux<FactureResponse> findByOrganizationId(UUID organizationId);

    Flux<FactureResponse> findByAgencyId(UUID agencyId);

    Flux<FactureResponse> findByCreatedBy(UUID sellerId);
}
