package com.example.account.modules.core.adapter.output.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserR2dbcRepository extends R2dbcRepository<UserPersistenceEntity, UUID> {

    Mono<UserPersistenceEntity> findByUsername(String username);

    Mono<UserPersistenceEntity> findByEmail(String email);

    @Query("SELECT * FROM users WHERE username = :identifier OR email = :identifier")
    Mono<UserPersistenceEntity> findByUsernameOrEmail(String identifier);

    Mono<Boolean> existsByUsername(String username);

    Mono<Boolean> existsByEmail(String email);

    Mono<UserPersistenceEntity> findByUsernameAndIsActiveTrue(String username);

    Mono<UserPersistenceEntity> findByEmailAndIsActiveTrue(String email);

    @Query("SELECT * FROM users WHERE id = :userId")
    Mono<UserPersistenceEntity> findByIdWithOrganizations(UUID userId);

    @Query("SELECT * FROM users WHERE username = :username")
    Mono<UserPersistenceEntity> findByUsernameWithOrganizations(String username);
}
