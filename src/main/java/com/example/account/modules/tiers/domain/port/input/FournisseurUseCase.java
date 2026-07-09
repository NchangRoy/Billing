package com.example.account.modules.tiers.domain.port.input;

import com.example.account.modules.tiers.dto.FournisseurCreateRequest;
import com.example.account.modules.tiers.dto.FournisseurUpdateRequest;
import com.example.account.modules.tiers.dto.FournisseurResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FournisseurUseCase {
    Mono<FournisseurResponse> createFournisseur(FournisseurCreateRequest request);
    Mono<FournisseurResponse> updateFournisseur(UUID fournisseurId, FournisseurUpdateRequest request);
    Mono<FournisseurResponse> getFournisseurById(UUID fournisseurId);
    Mono<FournisseurResponse> getFournisseurByUsername(String username);
    Flux<FournisseurResponse> getAllFournisseurs();
    Flux<FournisseurResponse> getActiveFournisseurs();
    Flux<FournisseurResponse> getFournisseursByCategorie(String categorie);
    Mono<Void> deleteFournisseur(UUID fournisseurId);
    Mono<FournisseurResponse> updateSolde(UUID fournisseurId, Double montant);
    Mono<FournisseurResponse> desactiverFournisseur(UUID fournisseurId);
    Mono<FournisseurResponse> activerFournisseur(UUID fournisseurId);
    Mono<Long> countActiveFournisseurs();
    Mono<Void> inviteFournisseur(UUID fournisseurId, String email, String name);
    Mono<Void> ensureFournisseurPortalAccess(UUID fournisseurId, String email, String name);
}
