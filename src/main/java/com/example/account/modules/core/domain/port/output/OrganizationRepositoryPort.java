package com.example.account.modules.core.domain.port.output;

import com.example.account.modules.core.domain.model.Organization;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrganizationRepositoryPort {
    Mono<Organization> findById(UUID id);
    Mono<Organization> findByCode(String code);
    Mono<Boolean> existsByCode(String code);
    Mono<Organization> save(Organization organization);
    Flux<Organization> findAll();
}
