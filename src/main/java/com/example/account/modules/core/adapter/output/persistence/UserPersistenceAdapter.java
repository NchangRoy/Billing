package com.example.account.modules.core.adapter.output.persistence;

import com.example.account.modules.core.domain.model.User;
import com.example.account.modules.core.domain.port.output.UserRepositoryPort;
import com.example.account.modules.core.adapter.output.persistence.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserR2dbcRepository repository;
    private final UserPersistenceMapper mapper;

    @Override
    public Mono<User> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return repository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Mono<User> findByUsernameOrEmail(String identifier) {
        return repository.findByUsernameOrEmail(identifier).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Mono<User> findByUsernameAndIsActiveTrue(String username) {
        return repository.findByUsernameAndIsActiveTrue(username).map(mapper::toDomain);
    }

    @Override
    public Mono<User> findByEmailAndIsActiveTrue(String email) {
        return repository.findByEmailAndIsActiveTrue(email).map(mapper::toDomain);
    }

    @Override
    public Mono<User> findByIdWithOrganizations(UUID userId) {
        return repository.findByIdWithOrganizations(userId).map(mapper::toDomain);
    }

    @Override
    public Mono<User> findByUsernameWithOrganizations(String username) {
        return repository.findByUsernameWithOrganizations(username).map(mapper::toDomain);
    }
    
    @Override
    public Mono<User> save(User user) {
        return repository.save(mapper.toEntity(user)).map(mapper::toDomain);
    }
}
