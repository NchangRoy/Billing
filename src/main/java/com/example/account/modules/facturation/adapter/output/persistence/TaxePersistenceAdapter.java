package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.domain.model.Taxes;
import com.example.account.modules.facturation.domain.port.output.TaxeRepositoryPort;
import com.example.account.modules.facturation.adapter.output.persistence.mapper.TaxePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaxePersistenceAdapter implements TaxeRepositoryPort {

    private final TaxeR2dbcRepository repository;
    private final TaxePersistenceMapper mapper;
    private final org.springframework.data.r2dbc.core.R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<Taxes> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<Taxes> findByNomTaxe(String nomTaxe) {
        return repository.findByNomTaxe(nomTaxe).map(mapper::toDomain);
    }

    @Override
    public Flux<Taxes> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }

    @Override
    public Flux<Taxes> findAllActiveTaxes() {
        return repository.findAllActiveTaxes().map(mapper::toDomain);
    }

    @Override
    public Flux<Taxes> findByTypeTaxe(String typeTaxe) {
        return repository.findByTypeTaxe(typeTaxe).map(mapper::toDomain);
    }

    @Override
    public Flux<Taxes> findActiveByTypeTaxe(String typeTaxe) {
        return repository.findActiveByTypeTaxe(typeTaxe).map(mapper::toDomain);
    }

    @Override
    public Flux<Taxes> findByPorteTaxe(String porteTaxe) {
        return repository.findByPorteTaxe(porteTaxe).map(mapper::toDomain);
    }

    @Override
    public Flux<Taxes> findByPositionFiscale(String positionFiscale) {
        return repository.findByPositionFiscale(positionFiscale).map(mapper::toDomain);
    }

    @Override
    public Flux<Taxes> findByCalculTaxeBetween(BigDecimal min, BigDecimal max) {
        return repository.findByCalculTaxeBetween(min, max).map(mapper::toDomain);
    }

    @Override
    public Flux<Taxes> findByMontantBetween(BigDecimal min, BigDecimal max) {
        return repository.findByMontantBetween(min, max).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNomTaxe(String nomTaxe) {
        return repository.existsByNomTaxe(nomTaxe);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Flux<Taxes> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }

    @Override
    public Flux<Taxes> findByAgencyId(UUID agencyId) {
        return repository.findByAgencyId(agencyId).map(mapper::toDomain);
    }

    @Override
    public Mono<Taxes> save(Taxes taxe) {
        TaxePersistenceEntity entity = mapper.toEntity(taxe);
        if (entity.getIdTaxe() == null) {
            entity.setIdTaxe(UUID.randomUUID());
            return entityTemplate.insert(entity).map(mapper::toDomain);
        }
        return repository.existsById(entity.getIdTaxe())
                .flatMap(exists -> exists ? repository.save(entity) : entityTemplate.insert(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Long> countActiveTaxes() {
        return repository.countActiveTaxes();
    }

    @Override
    public Mono<Long> countByTypeTaxe(String typeTaxe) {
        return repository.countByTypeTaxe(typeTaxe);
    }
}
