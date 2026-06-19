package com.example.account.modules.facturation.adapter.output.persistence;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface BackOrderR2dbcRepository extends R2dbcRepository<BackOrderPersistenceEntity, UUID> {
    Flux<BackOrderPersistenceEntity> findByOrganizationId(UUID organizationId);
    Flux<BackOrderPersistenceEntity> findByAgencyId(UUID agencyId);
    Flux<BackOrderPersistenceEntity> findByIdBonAchat(UUID idBonAchat);
    Flux<BackOrderPersistenceEntity> findByIdFournisseur(UUID idFournisseur);
}
