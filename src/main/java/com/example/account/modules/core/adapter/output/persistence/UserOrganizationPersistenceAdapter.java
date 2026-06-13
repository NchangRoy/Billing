package com.example.account.modules.core.adapter.output.persistence;

import com.example.account.modules.core.domain.model.UserOrganization;
import com.example.account.modules.core.domain.port.output.UserOrganizationRepositoryPort;
import com.example.account.modules.core.adapter.output.persistence.mapper.UserOrganizationPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserOrganizationPersistenceAdapter implements UserOrganizationRepositoryPort {

    private final UserOrganizationR2dbcRepository repository;
    private final UserOrganizationPersistenceMapper mapper;

    @Override
    public Flux<UserOrganization> findByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .map(mapper::toDomain); // Note: we should map to the old entity or domain depending on what Port uses
    }

    @Override
    public Flux<UserOrganization> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<UserOrganization> findByUserIdAndOrganizationId(UUID userId, UUID organizationId) {
        return repository.findByUserIdAndOrganizationId(userId, organizationId)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<UserOrganization> findActiveByUserId(UUID userId) {
        return repository.findActiveByUserId(userId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<UserOrganization> findDefaultByUserId(UUID userId) {
        return repository.findDefaultByUserId(userId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByUserIdAndOrganizationId(UUID userId, UUID organizationId) {
        return repository.existsByUserIdAndOrganizationId(userId, organizationId);
    }

    @Override
    public Mono<UserOrganization> save(UserOrganization userOrganization) {
        return repository.save(mapper.toEntity(userOrganization))
                .map(mapper::toDomain);
    }
}
