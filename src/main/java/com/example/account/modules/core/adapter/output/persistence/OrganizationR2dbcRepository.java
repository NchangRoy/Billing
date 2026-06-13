package com.example.account.modules.core.adapter.output.persistence;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface OrganizationR2dbcRepository extends R2dbcRepository<OrganizationPersistenceEntity, UUID> {
    Mono<OrganizationPersistenceEntity> findByCode(String code);
    Mono<Boolean> existsByCode(String code);
}
