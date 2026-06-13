package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.model.enums.StatutFacture;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface FactureR2dbcRepository extends R2dbcRepository<FacturePersistenceEntity, UUID> {
    
    Mono<FacturePersistenceEntity> findByNumeroFacture(String numeroFacture);
    
    Flux<FacturePersistenceEntity> findByIdClient(UUID idClient);
    
    Flux<FacturePersistenceEntity> findByEtat(StatutFacture etat);
    
    Flux<FacturePersistenceEntity> findByType(String type);
    
    @Query("SELECT * FROM factures WHERE id_client = :idClient AND etat = :etat")
    Flux<FacturePersistenceEntity> findByClientAndEtat(UUID idClient, StatutFacture etat);
    
    @Query("SELECT * FROM factures WHERE date_facturation BETWEEN :startDate AND :endDate")
    Flux<FacturePersistenceEntity> findByDateFacturationBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT * FROM factures WHERE date_echeance BETWEEN :startDate AND :endDate")
    Flux<FacturePersistenceEntity> findByDateEcheanceBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT * FROM factures WHERE date_echeance < :currentDate AND (etat = 'ENVOYE' OR etat = 'PARTIELLEMENT_PAYE')")
    Flux<FacturePersistenceEntity> findOverdueFactures(LocalDate currentDate);
    
    @Query("SELECT * FROM factures WHERE montant_total BETWEEN :minAmount AND :maxAmount")
    Flux<FacturePersistenceEntity> findByMontantTotalBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    @Query("SELECT * FROM factures WHERE montant_restant > 0")
    Flux<FacturePersistenceEntity> findUnpaidFactures();
    
    @Query("SELECT * FROM factures WHERE devise = :devise")
    Flux<FacturePersistenceEntity> findByDevise(String devise);
    
    @Query("SELECT * FROM factures WHERE envoye_par_email = :envoyeParEmail")
    Flux<FacturePersistenceEntity> findByEnvoyeParEmail(Boolean envoyeParEmail);
    
    @Query("SELECT COUNT(*) FROM factures WHERE etat = :etat")
    Mono<Long> countByEtat(StatutFacture etat);
    
    @Query("SELECT COUNT(*) FROM factures WHERE id_client = :idClient")
    Mono<Long> countByIdClient(UUID idClient);
    
    @Query("SELECT COUNT(*) FROM factures WHERE date_facturation BETWEEN :startDate AND :endDate")
    Mono<Long> countByDateFacturationBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(montant_total) FROM factures WHERE date_facturation BETWEEN :startDate AND :endDate")
    Mono<BigDecimal> sumMontantByDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(montant_total) FROM factures WHERE etat = :etat")
    Mono<BigDecimal> sumMontantByEtat(StatutFacture etat);
    
    @Query("SELECT COUNT(*) FROM factures WHERE CAST(etat AS TEXT) = :statut")
    Mono<Long> countByStatut(String statut);
    
    @Query("SELECT SUM(montant_total) FROM factures WHERE CAST(etat AS TEXT) = :statut")
    Mono<BigDecimal> sumMontantByStatut(String statut);
    
    @Query("SELECT COUNT(*) FROM factures WHERE date_facturation >= :startDate AND date_facturation <= :endDate")
    Mono<Long> countByDateBetween(LocalDate startDate, LocalDate endDate);
    
    Mono<Boolean> existsByNumeroFacture(String numeroFacture);
}
