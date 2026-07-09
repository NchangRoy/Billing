package com.example.account.modules.facturation.repository;

import com.example.account.modules.facturation.model.entity.BonLivraison;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface BonLivraisonRepository extends R2dbcRepository<BonLivraison, UUID> {
    Mono<BonLivraison> findByNumeroLivraison(String numeroLivraison);
    Flux<BonLivraison> findByIdClient(UUID idClient);
    Flux<BonLivraison> findByOrganizationId(UUID organizationId);
    Flux<BonLivraison> findByAgencyId(UUID agencyId);
    Flux<BonLivraison> findByCreatedBy(UUID createdBy);
}
