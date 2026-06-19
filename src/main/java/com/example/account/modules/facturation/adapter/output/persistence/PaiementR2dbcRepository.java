package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.model.enums.TypePaiement;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface PaiementR2dbcRepository extends R2dbcRepository<PaiementPersistenceEntity, UUID> {
    Flux<PaiementPersistenceEntity> findByIdClient(UUID idClient);
    Flux<PaiementPersistenceEntity> findByIdFacture(UUID idFacture);
    Flux<PaiementPersistenceEntity> findByModePaiement(TypePaiement mode);
    Flux<PaiementPersistenceEntity> findByDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT SUM(montant) FROM paiements WHERE id_client = :idClient")
    Mono<BigDecimal> sumMontantByClient(UUID idClient);

    @Query("SELECT SUM(montant) FROM paiements WHERE id_facture = :idFacture")
    Mono<BigDecimal> sumMontantByFacture(UUID idFacture);

    @Query("SELECT SUM(montant) FROM paiements WHERE date BETWEEN :start AND :end")
    Mono<BigDecimal> sumMontantByDateBetween(LocalDate start, LocalDate end);

    Mono<Long> countByIdClient(UUID idClient);
    Mono<Long> countByModePaiement(TypePaiement mode);

    Flux<PaiementPersistenceEntity> findByOrganizationId(UUID organizationId);

    Flux<PaiementPersistenceEntity> findByAgencyId(UUID agencyId);
}
