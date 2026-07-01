package com.example.account.modules.tiers.controller;

import com.example.account.modules.tiers.domain.port.input.ClientUseCase;
import com.example.account.modules.tiers.dto.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/tiers/clients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clients", description = "Gestion des clients (tiers CUSTOMER depuis le Kernel)")
public class ClientController {

    private final ClientUseCase clientUseCase;

    @GetMapping
    @Operation(summary = "Lister tous les clients actifs")
    public Flux<ClientResponse> getAllClients() {
        log.info("GET /api/tiers/clients");
        return clientUseCase.getActiveClients();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un client par ID")
    public Mono<ResponseEntity<ClientResponse>> getClientById(@PathVariable UUID id) {
        log.info("GET /api/tiers/clients/{}", id);
        return clientUseCase.getClientById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    @Operation(summary = "Nombre de clients actifs")
    public Mono<ResponseEntity<Long>> countClients() {
        return clientUseCase.countActiveClients().map(ResponseEntity::ok);
    }
}
