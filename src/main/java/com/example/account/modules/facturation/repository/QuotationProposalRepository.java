package com.example.account.modules.facturation.repository;

import com.example.account.modules.facturation.model.entity.QuotationProposal;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface QuotationProposalRepository extends R2dbcRepository<QuotationProposal, UUID> {
    Flux<QuotationProposal> findByOrganizationId(UUID organizationId);
    Flux<QuotationProposal> findByIdClientAndOrganizationId(UUID idClient, UUID organizationId);
}
