package com.example.account.modules.tiers.domain.port.input;

import com.example.account.modules.tiers.dto.ClientCreateRequest;
import com.example.account.modules.tiers.dto.ClientUpdateRequest;
import com.example.account.modules.tiers.dto.ClientResponse;
import com.example.account.modules.tiers.domain.model.enums.TypeClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ClientUseCase {
    Mono<ClientResponse> createClient(ClientCreateRequest request);
    Mono<ClientResponse> updateClient(UUID clientId, ClientUpdateRequest request);
    Mono<ClientResponse> getClientById(UUID clientId);
    Mono<ClientResponse> getClientByUsername(String username);
    Flux<ClientResponse> getAllClients();
    Flux<ClientResponse> getActiveClients();
    Flux<ClientResponse> getClientsByType(TypeClient typeClient);
    Mono<Void> deleteClient(UUID clientId);
    Mono<ClientResponse> updateSolde(UUID clientId, Double montant);
    Mono<Long> countActiveClients();
}
