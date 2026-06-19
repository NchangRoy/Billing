package com.example.account.modules.facturation.repository;

import com.example.account.modules.facturation.model.entity.FactureFournisseur;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface FactureFournisseurRepository extends R2dbcRepository<FactureFournisseur, UUID> {
    Mono<FactureFournisseur> findByNumeroFacture(String numeroFacture);
    Flux<FactureFournisseur> findByIdFournisseur(UUID idFournisseur);
    Mono<Boolean> existsByNumeroFacture(String numeroFacture);
    Flux<FactureFournisseur> findByOrganizationId(UUID organizationId);
    Flux<FactureFournisseur> findByAgencyId(UUID agencyId);
}
