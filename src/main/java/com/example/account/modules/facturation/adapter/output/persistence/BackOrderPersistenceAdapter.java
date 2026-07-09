package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.adapter.output.persistence.mapper.BackOrderPersistenceMapper;
import com.example.account.modules.facturation.domain.model.BackOrder;
import com.example.account.modules.facturation.domain.port.output.BackOrderRepositoryPort;
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
public class BackOrderPersistenceAdapter implements BackOrderRepositoryPort {

    private final BackOrderR2dbcRepository repository;
    private final BackOrderPersistenceMapper mapper;
    private final R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<BackOrder> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<BackOrder> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }

    @Override
    public Flux<BackOrder> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }

    @Override
    public Flux<BackOrder> findByAgencyId(UUID agencyId) {
        return repository.findByAgencyId(agencyId).map(mapper::toDomain);
    }

    @Override
    public Flux<BackOrder> findByIdBonLivraison(UUID idBonLivraison) {
        return repository.findByIdBonLivraison(idBonLivraison).map(mapper::toDomain);
    }

    @Override
    public Flux<BackOrder> findByIdClient(UUID idClient) {
        return repository.findByIdClient(idClient).map(mapper::toDomain);
    }

    @Override
    public Flux<BackOrder> findByCreatedBy(UUID createdBy) {
        return repository.findByCreatedBy(createdBy).map(mapper::toDomain);
    }

    @Override
    public Mono<BackOrder> insert(BackOrder backOrder) {
        return entityTemplate.insert(mapper.toEntity(backOrder)).map(mapper::toDomain);
    }

    @Override
    public Mono<BackOrder> save(BackOrder backOrder) {
        return repository.save(mapper.toEntity(backOrder)).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Void> delete(BackOrder backOrder) {
        return repository.delete(mapper.toEntity(backOrder));
    }
}
