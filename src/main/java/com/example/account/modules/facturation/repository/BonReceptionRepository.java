package com.example.account.modules.facturation.repository;

import com.example.account.modules.facturation.model.entity.BondeReception;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface BonReceptionRepository extends R2dbcRepository<BondeReception, UUID> {
    Mono<BondeReception> findByNumeroReception(String numeroReception);
    Flux<BondeReception> findByIdFournisseur(UUID idFournisseur);
    Flux<BondeReception> findByOrganizationId(UUID organizationId);
    Flux<BondeReception> findByAgencyId(UUID agencyId);
    Flux<BondeReception> findByCreatedBy(UUID createdBy);
}
