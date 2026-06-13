package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.domain.model.FactureProforma;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FactureProformaRepositoryPort {
    Mono<FactureProforma> findById(UUID id);
    Mono<FactureProforma> findByNumeroProformaInvoice(String numeroProformaInvoice);
    Flux<FactureProforma> findByIdClient(UUID idClient);
    Mono<Boolean> existsByNumeroProformaInvoice(String numeroProformaInvoice);
    Mono<Boolean> existsById(UUID id);
    Mono<Void> deleteById(UUID id);
    Mono<FactureProforma> save(FactureProforma proforma);
    Mono<FactureProforma> insert(FactureProforma proforma);
    Flux<FactureProforma> findAll();
}
