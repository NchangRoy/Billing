package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.model.enums.StatutDevis;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface DevisR2dbcRepository extends R2dbcRepository<DevisPersistenceEntity, UUID> {
    Mono<DevisPersistenceEntity> findByNumeroDevis(String numeroDevis);
    Flux<DevisPersistenceEntity> findByStatut(StatutDevis statut);
    Mono<Boolean> existsByNumeroDevis(String numeroDevis);
    
    @Query("SELECT * FROM devis WHERE id_client = :idClient")
    Flux<DevisPersistenceEntity> findByIdClient(UUID idClient);

    @Query("SELECT * FROM devis WHERE date_expiration < :date AND statut != 'EXPIRE'")
    Flux<DevisPersistenceEntity> findExpiredDevis(LocalDate date);

    Flux<DevisPersistenceEntity> findByDateCreationBetween(LocalDate start, LocalDate end);
    
    Flux<DevisPersistenceEntity> findByOrganizationId(UUID organizationId);

    Flux<DevisPersistenceEntity> findByAgencyId(UUID agencyId);

    Flux<DevisPersistenceEntity> findByCreatedBy(UUID createdBy);
}
