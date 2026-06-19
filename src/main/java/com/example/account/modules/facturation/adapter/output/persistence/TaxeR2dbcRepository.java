package com.example.account.modules.facturation.adapter.output.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface TaxeR2dbcRepository extends R2dbcRepository<TaxePersistenceEntity, UUID> {
    Mono<TaxePersistenceEntity> findByNomTaxe(String nomTaxe);
    Flux<TaxePersistenceEntity> findByActifTrue();
    Flux<TaxePersistenceEntity> findByTypeTaxe(String typeTaxe);
    Mono<Boolean> existsByNomTaxe(String nomTaxe);
    
    @Query("SELECT * FROM taxes WHERE actif = true")
    Flux<TaxePersistenceEntity> findAllActiveTaxes();
    
    @Query("SELECT * FROM taxes WHERE type_taxe = :typeTaxe AND actif = true")
    Flux<TaxePersistenceEntity> findActiveByTypeTaxe(String typeTaxe);
    
    Flux<TaxePersistenceEntity> findByPorteTaxe(String porteTaxe);
    Flux<TaxePersistenceEntity> findByPositionFiscale(String positionFiscale);
    Flux<TaxePersistenceEntity> findByCalculTaxeBetween(BigDecimal minTaux, BigDecimal maxTaux);
    Flux<TaxePersistenceEntity> findByMontantBetween(BigDecimal minMontant, BigDecimal maxMontant);
    
    @Query("SELECT COUNT(*) FROM taxes WHERE actif = true")
    Mono<Long> countActiveTaxes();
    
    @Query("SELECT COUNT(*) FROM taxes WHERE type_taxe = :typeTaxe")
    Mono<Long> countByTypeTaxe(String typeTaxe);

    Flux<TaxePersistenceEntity> findByOrganizationId(UUID organizationId);

    Flux<TaxePersistenceEntity> findByAgencyId(UUID agencyId);
}
