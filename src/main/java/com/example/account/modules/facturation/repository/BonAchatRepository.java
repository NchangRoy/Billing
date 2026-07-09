package com.example.account.modules.facturation.repository;

import com.example.account.modules.facturation.model.entity.BonAchat;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface BonAchatRepository extends R2dbcRepository<BonAchat, UUID> {
    Mono<BonAchat> findByNumeroBonAchat(String numeroBonAchat);
    Flux<BonAchat> findByIdFournisseur(UUID idFournisseur);
    Flux<BonAchat> findByOrganizationId(UUID organizationId);
    Flux<BonAchat> findByAgencyId(UUID agencyId);
    Flux<BonAchat> findByCreatedBy(UUID createdBy);
}
