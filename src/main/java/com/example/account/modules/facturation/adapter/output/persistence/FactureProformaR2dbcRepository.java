package com.example.account.modules.facturation.adapter.output.persistence;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface FactureProformaR2dbcRepository extends R2dbcRepository<FactureProformaPersistenceEntity, UUID> {
    Mono<FactureProformaPersistenceEntity> findByNumeroProformaInvoice(String numeroProformaInvoice);
    Flux<FactureProformaPersistenceEntity> findByIdClient(UUID idClient);
    Mono<Boolean> existsByNumeroProformaInvoice(String numeroProformaInvoice);
}
