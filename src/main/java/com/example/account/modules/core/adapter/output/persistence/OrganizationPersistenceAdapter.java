package com.example.account.modules.core.adapter.output.persistence;

import com.example.account.modules.core.domain.model.Organization;
import com.example.account.modules.core.domain.port.output.OrganizationRepositoryPort;
import com.example.account.modules.core.adapter.output.persistence.mapper.OrganizationPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrganizationPersistenceAdapter implements OrganizationRepositoryPort {

    private final OrganizationR2dbcRepository repository;
    private final OrganizationPersistenceMapper mapper;

    @Override
    public Mono<Organization> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<Organization> findByCode(String code) {
        return repository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public Mono<Organization> save(Organization organization) {
        return repository.save(mapper.toEntity(organization)).map(mapper::toDomain);
    }

    @Override
    public Flux<Organization> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }
}
