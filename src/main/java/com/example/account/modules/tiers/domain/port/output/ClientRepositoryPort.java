package com.example.account.modules.tiers.domain.port.output;

import com.example.account.modules.tiers.domain.model.Client;
import com.example.account.modules.tiers.domain.model.enums.TypeClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ClientRepositoryPort {
    Mono<Client> findById(UUID id);
    Mono<Client> findByUsername(String username);
    Mono<Client> findByEmail(String email);
    Mono<Client> findByCodeClient(String codeClient);
    Flux<Client> findByTypeClient(TypeClient typeClient);
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
    Flux<Client> findAllActiveClients();
    Mono<Long> countActiveClients();
    Mono<Client> save(Client client);
    Mono<Void> deleteById(UUID id);
    Mono<Boolean> existsById(UUID id);
    Mono<Long> count();
    Mono<Void> resendCredentials(UUID id, String email, String name);
    Mono<Void> ensurePortalAccess(UUID id, String email, String name);
}
