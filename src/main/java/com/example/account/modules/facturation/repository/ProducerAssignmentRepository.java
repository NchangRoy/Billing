package com.example.account.modules.facturation.repository;

import com.example.account.modules.facturation.model.entity.ProducerAssignment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ProducerAssignmentRepository extends R2dbcRepository<ProducerAssignment, UUID> {
    Mono<ProducerAssignment> findByFournisseurIdAndOrganizationId(UUID fournisseurId, UUID organizationId);
    Flux<ProducerAssignment> findByOrganizationId(UUID organizationId);
    Flux<ProducerAssignment> findBySellerIdAndOrganizationId(UUID sellerId, UUID organizationId);
    Mono<Void> deleteByFournisseurIdAndOrganizationId(UUID fournisseurId, UUID organizationId);
}
