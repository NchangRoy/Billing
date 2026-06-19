package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.adapter.output.persistence.mapper.FactureProformaPersistenceMapper;
import com.example.account.modules.facturation.domain.model.FactureProforma;
import com.example.account.modules.facturation.domain.port.output.FactureProformaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class FactureProformaPersistenceAdapter implements FactureProformaRepositoryPort {

    private final FactureProformaR2dbcRepository repository;
    private final FactureProformaPersistenceMapper mapper;
    private final R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<FactureProforma> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<FactureProforma> findByNumeroProformaInvoice(String numeroProformaInvoice) {
        return repository.findByNumeroProformaInvoice(numeroProformaInvoice).map(mapper::toDomain);
    }

    @Override
    public Flux<FactureProforma> findByIdClient(UUID idClient) {
        return repository.findByIdClient(idClient).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNumeroProformaInvoice(String numeroProformaInvoice) {
        return repository.existsByNumeroProformaInvoice(numeroProformaInvoice);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<FactureProforma> save(FactureProforma proforma) {
        return repository.save(mapper.toEntity(proforma)).map(mapper::toDomain);
    }

    @Override
    public Mono<FactureProforma> insert(FactureProforma proforma) {
        return entityTemplate.insert(mapper.toEntity(proforma)).map(mapper::toDomain);
    }

    @Override
    public Flux<FactureProforma> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }

    @Override
    public Flux<FactureProforma> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }

    @Override
    public Flux<FactureProforma> findByAgencyId(UUID agencyId) {
        return repository.findByAgencyId(agencyId).map(mapper::toDomain);
    }
}
