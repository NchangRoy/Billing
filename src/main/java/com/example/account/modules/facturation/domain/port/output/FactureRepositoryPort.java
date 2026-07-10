package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.domain.model.Facture;
import com.example.account.modules.facturation.model.enums.StatutFacture;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public interface FactureRepositoryPort {
    Mono<Map<String, Object>> getAccountingSaleFacture(UUID factureId);
    Mono<Map<String, Object>> getAccountingPurchaseFacture(UUID factureId);

    Mono<Facture> findById(UUID id);
    Mono<Facture> findByNumeroFacture(String numeroFacture);
    Flux<Facture> findByIdClient(UUID idClient);
    Flux<Facture> findByEtat(StatutFacture etat);
    Flux<Facture> findByType(String type);
    Flux<Facture> findByClientAndEtat(UUID idClient, StatutFacture etat);
    Flux<Facture> findByDateFacturationBetween(LocalDate startDate, LocalDate endDate);
    Flux<Facture> findByDateEcheanceBetween(LocalDate startDate, LocalDate endDate);
    Flux<Facture> findOverdueFactures(LocalDate currentDate);
    Flux<Facture> findByMontantTotalBetween(BigDecimal minAmount, BigDecimal maxAmount);
    Flux<Facture> findUnpaidFactures();
    Flux<Facture> findByDevise(String devise);
    Flux<Facture> findByEnvoyeParEmail(Boolean envoyeParEmail);
    Mono<Long> countByEtat(StatutFacture etat);
    Mono<Long> countByIdClient(UUID idClient);
    Mono<Long> countByDateFacturationBetween(LocalDate startDate, LocalDate endDate);
    Mono<BigDecimal> sumMontantByDateBetween(LocalDate startDate, LocalDate endDate);
    Mono<BigDecimal> sumMontantByEtat(StatutFacture etat);
    Mono<Long> countByStatut(String statut);
    Mono<BigDecimal> sumMontantByStatut(String statut);
    Mono<Long> countByDateBetween(LocalDate startDate, LocalDate endDate);
    Mono<Boolean> existsByNumeroFacture(String numeroFacture);
    Flux<Facture> findByOrganizationId(UUID organizationId);
    Flux<Facture> findByAgencyId(UUID agencyId);
    Flux<Facture> findByCreatedBy(UUID createdBy);
    Mono<Facture> save(Facture facture);
    Mono<Facture> insert(Facture facture);
    Mono<Void> deleteById(UUID id);
    Mono<Boolean> existsById(UUID id);
    Flux<Facture> findAll();
    Mono<Long> count();
}
