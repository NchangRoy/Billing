package com.example.account.modules.facturation.repository;

import com.example.account.modules.facturation.model.entity.CustomerAssignment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CustomerAssignmentRepository extends R2dbcRepository<CustomerAssignment, UUID> {
    Mono<CustomerAssignment> findByClientIdAndOrganizationId(UUID clientId, UUID organizationId);
    Flux<CustomerAssignment> findByOrganizationId(UUID organizationId);
    Flux<CustomerAssignment> findBySellerIdAndOrganizationId(UUID sellerId, UUID organizationId);
    Mono<Void> deleteByClientIdAndOrganizationId(UUID clientId, UUID organizationId);
}
