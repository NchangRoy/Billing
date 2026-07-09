package com.example.account.modules.tiers.domain.port.output;

import com.example.account.modules.tiers.domain.model.Fournisseur;
import com.example.account.modules.tiers.domain.model.enums.TypeClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FournisseurRepositoryPort {
    Mono<Fournisseur> findById(UUID id);
    Mono<Fournisseur> findByUsername(String username);
    Mono<Fournisseur> findByEmail(String email);
    Mono<Fournisseur> findByCodeFournisseur(String codeFournisseur);
    Flux<Fournisseur> findByTypeFournisseur(TypeClient typeFournisseur);
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
    Flux<Fournisseur> findAllActiveFournisseurs();
    Mono<Long> countActiveFournisseurs();
    Mono<Fournisseur> save(Fournisseur fournisseur);
    Mono<Void> deleteById(UUID id);
    Mono<Boolean> existsById(UUID id);
    Mono<Void> resendCredentials(UUID id, String email, String name);
    Mono<Void> ensurePortalAccess(UUID id, String email, String name);
}
