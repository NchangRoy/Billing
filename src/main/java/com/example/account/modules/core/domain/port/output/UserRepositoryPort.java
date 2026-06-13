package com.example.account.modules.core.domain.port.output;

import com.example.account.modules.core.domain.model.User;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepositoryPort {
    Mono<User> findById(UUID id);
    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);
    Mono<User> findByUsernameOrEmail(String identifier);
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
    Mono<User> findByUsernameAndIsActiveTrue(String username);
    Mono<User> findByEmailAndIsActiveTrue(String email);
    Mono<User> findByIdWithOrganizations(UUID userId);
    Mono<User> findByUsernameWithOrganizations(String username);
    Mono<User> save(User user);
}
