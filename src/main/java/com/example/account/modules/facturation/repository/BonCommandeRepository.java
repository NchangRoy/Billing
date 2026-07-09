package com.example.account.modules.facturation.repository;

import com.example.account.modules.facturation.model.entity.BonCommande;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface BonCommandeRepository extends R2dbcRepository<BonCommande, UUID> {
    Mono<BonCommande> findByNumeroCommande(String numeroCommande);
    Flux<BonCommande> findByIdClient(UUID idClient);
    Mono<Boolean> existsByNumeroCommande(String numeroCommande);
    Flux<BonCommande> findByOrganizationId(UUID organizationId);
    Flux<BonCommande> findByIdAgency(UUID idAgency);
    Flux<BonCommande> findByCreatedBy(UUID createdBy);
}
