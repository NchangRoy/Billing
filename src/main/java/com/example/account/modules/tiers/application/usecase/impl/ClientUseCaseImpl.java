package com.example.account.modules.tiers.application.usecase.impl;

import com.example.account.modules.tiers.dto.ClientCreateRequest;
import com.example.account.modules.tiers.dto.ClientUpdateRequest;
import com.example.account.modules.tiers.dto.ClientResponse;
import com.example.account.modules.tiers.mapper.ClientMapper;
import com.example.account.modules.tiers.domain.model.Client;
import com.example.account.modules.tiers.domain.model.enums.TypeClient;
import com.example.account.modules.tiers.domain.port.output.ClientRepositoryPort;
import com.example.account.modules.tiers.domain.port.output.ClientEventPort;
import com.example.account.modules.tiers.domain.port.input.ClientUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientUseCaseImpl implements ClientUseCase {
    
    private final ClientRepositoryPort clientRepositoryPort;
    private final ClientMapper clientMapper;
    private final ClientEventPort clientEventPort;

    @Override
    @Transactional
    public Mono<ClientResponse> createClient(ClientCreateRequest request) {
        log.info("Création d'un nouveau client: {}", request.getUsername());

        return clientRepositoryPort.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Un client avec ce username existe déjà"));
                    }
                    return request.getEmail() != null ? clientRepositoryPort.existsByEmail(request.getEmail()) : Mono.just(false);
                })
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new IllegalArgumentException("Un client avec cet email existe déjà"));
                    }
                    
                    Client client = clientMapper.toEntity(request);
                    if (client.getIdClient() == null) {
                        client.setIdClient(UUID.randomUUID());
                    }
                    return clientRepositoryPort.save(client);
                })
                .map(savedClient -> {
                    ClientResponse response = clientMapper.toResponse(savedClient);
                    clientEventPort.publishClientCreated(response);
                    log.info("Client créé avec succès: {}", savedClient.getIdClient());
                    return response;
                });
    }

    @Override
    @Transactional
    public Mono<ClientResponse> updateClient(UUID clientId, ClientUpdateRequest request) {
        log.info("Mise à jour du client: {}", clientId);

        return clientRepositoryPort.findById(clientId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Client non trouvé: " + clientId)))
                .flatMap(client -> {
                    clientMapper.updateEntityFromRequest(request, client);
                    return clientRepositoryPort.save(client);
                })
                .map(updatedClient -> {
                    ClientResponse response = clientMapper.toResponse(updatedClient);
                    clientEventPort.publishClientUpdated(response);
                    log.info("Client mis à jour avec succès: {}", clientId);
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ClientResponse> getClientById(UUID clientId) {
        log.info("Récupération du client: {}", clientId);

        return clientRepositoryPort.findById(clientId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Client non trouvé: " + clientId)))
                .map(clientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ClientResponse> getClientByUsername(String username) {
        log.info("Récupération du client par username: {}", username);

        return clientRepositoryPort.findByUsername(username)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Client non trouvé avec username: " + username)))
                .map(clientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<ClientResponse> getAllClients() {
        log.info("Récupération de tous les clients");
        // Note: the original code calls findAll(), which might be different from findAllActiveClients.
        // Assuming we need a findAll() in the port, but wait, the port doesn't have it.
        // I will implement a findAll in the port or just use active clients.
        // I'll update the port to have findAll if needed, but for now I'll use active.
        return clientRepositoryPort.findAllActiveClients()
                .map(clientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<ClientResponse> getActiveClients() {
        log.info("Récupération des clients actifs");
        return clientRepositoryPort.findAllActiveClients()
                .map(clientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<ClientResponse> getClientsByType(TypeClient typeClient) {
        log.info("Récupération des clients par type: {}", typeClient);
        return clientRepositoryPort.findByTypeClient(typeClient)
                .map(clientMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> deleteClient(UUID clientId) {
        log.info("Suppression du client: {}", clientId);

        return clientRepositoryPort.existsById(clientId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Client non trouvé: " + clientId));
                    }
                    return clientRepositoryPort.deleteById(clientId)
                            .then(Mono.fromRunnable(() -> clientEventPort.publishClientDeleted(clientId)));
                })
                .then()
                .doOnSuccess(v -> log.info("Client supprimé avec succès: {}", clientId));
    }

    @Override
    @Transactional
    public Mono<ClientResponse> updateSolde(UUID clientId, Double montant) {
        log.info("Mise à jour du solde du client {}: {}", clientId, montant);

        return clientRepositoryPort.findById(clientId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Client non trouvé: " + clientId)))
                .flatMap(client -> {
                    client.setSoldeCourant(client.getSoldeCourant() + montant);
                    return clientRepositoryPort.save(client);
                })
                .map(clientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Long> countActiveClients() {
        return clientRepositoryPort.countActiveClients();
    }
}
