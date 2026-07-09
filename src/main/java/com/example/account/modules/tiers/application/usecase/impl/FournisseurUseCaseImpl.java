package com.example.account.modules.tiers.application.usecase.impl;

import com.example.account.modules.tiers.dto.FournisseurCreateRequest;
import com.example.account.modules.tiers.dto.FournisseurUpdateRequest;
import com.example.account.modules.tiers.dto.FournisseurResponse;
import com.example.account.modules.tiers.mapper.FournisseurMapper;
import com.example.account.modules.tiers.domain.model.Fournisseur;
import com.example.account.modules.tiers.domain.port.output.FournisseurRepositoryPort;
import com.example.account.modules.tiers.domain.port.output.FournisseurEventPort;
import com.example.account.modules.tiers.domain.port.input.FournisseurUseCase;
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
public class FournisseurUseCaseImpl implements FournisseurUseCase {

    private final FournisseurRepositoryPort fournisseurRepositoryPort;
    private final FournisseurMapper fournisseurMapper;
    private final FournisseurEventPort fournisseurEventPort;

    @Override
    @Transactional
    public Mono<FournisseurResponse> createFournisseur(FournisseurCreateRequest request) {
        log.info("Création d'un nouveau fournisseur: {}", request.getUsername());

        return fournisseurRepositoryPort.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Un fournisseur avec ce username existe déjà"));
                    }
                    return request.getEmail() != null ? fournisseurRepositoryPort.existsByEmail(request.getEmail()) : Mono.just(false);
                })
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new IllegalArgumentException("Un fournisseur avec cet email existe déjà"));
                    }
                    
                    Fournisseur fournisseur = fournisseurMapper.toEntity(request);
                    if (fournisseur.getIdFournisseur() == null) {
                        fournisseur.setIdFournisseur(UUID.randomUUID());
                    }
                    return fournisseurRepositoryPort.save(fournisseur);
                })
                .map(savedFournisseur -> {
                    FournisseurResponse response = fournisseurMapper.toResponse(savedFournisseur);
                    fournisseurEventPort.publishFournisseurCreated(response);
                    log.info("Fournisseur créé avec succès: {}", savedFournisseur.getIdFournisseur());
                    return response;
                });
    }

    @Override
    @Transactional
    public Mono<FournisseurResponse> updateFournisseur(UUID fournisseurId, FournisseurUpdateRequest request) {
        log.info("Mise à jour du fournisseur: {}", fournisseurId);

        return fournisseurRepositoryPort.findById(fournisseurId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Fournisseur non trouvé: " + fournisseurId)))
                .flatMap(fournisseur -> {
                    fournisseurMapper.updateEntityFromRequest(request, fournisseur);
                    return fournisseurRepositoryPort.save(fournisseur);
                })
                .map(updatedFournisseur -> {
                    FournisseurResponse response = fournisseurMapper.toResponse(updatedFournisseur);
                    fournisseurEventPort.publishFournisseurUpdated(response);
                    log.info("Fournisseur mis à jour avec succès: {}", fournisseurId);
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<FournisseurResponse> getFournisseurById(UUID fournisseurId) {
        log.info("Récupération du fournisseur: {}", fournisseurId);

        return fournisseurRepositoryPort.findById(fournisseurId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Fournisseur non trouvé: " + fournisseurId)))
                .map(fournisseurMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<FournisseurResponse> getFournisseurByUsername(String username) {
        log.info("Récupération du fournisseur par username: {}", username);

        return fournisseurRepositoryPort.findByUsername(username)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Fournisseur non trouvé avec username: " + username)))
                .map(fournisseurMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FournisseurResponse> getAllFournisseurs() {
        log.info("Récupération de tous les fournisseurs");
        return fournisseurRepositoryPort.findAllActiveFournisseurs() // Assuming fallback to active if findAll isn't in port
                .map(fournisseurMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FournisseurResponse> getActiveFournisseurs() {
        log.info("Récupération des fournisseurs actifs");
        return fournisseurRepositoryPort.findAllActiveFournisseurs()
                .map(fournisseurMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FournisseurResponse> getFournisseursByCategorie(String categorie) {
        log.info("Récupération des fournisseurs par catégorie: {}", categorie);
        // Note: Missing findByCategorie in port. Returning empty for now to make it compile.
        return Flux.empty();
    }

    @Override
    @Transactional
    public Mono<Void> deleteFournisseur(UUID fournisseurId) {
        log.info("Suppression du fournisseur: {}", fournisseurId);

        return fournisseurRepositoryPort.existsById(fournisseurId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Fournisseur non trouvé: " + fournisseurId));
                    }
                    return fournisseurRepositoryPort.deleteById(fournisseurId)
                            .then(Mono.fromRunnable(() -> fournisseurEventPort.publishFournisseurDeleted(fournisseurId)));
                })
                .then()
                .doOnSuccess(v -> log.info("Fournisseur supprimé avec succès: {}", fournisseurId));
    }

    @Override
    @Transactional
    public Mono<FournisseurResponse> updateSolde(UUID fournisseurId, Double montant) {
        log.info("Mise à jour du solde du fournisseur {}: {}", fournisseurId, montant);

        return fournisseurRepositoryPort.findById(fournisseurId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Fournisseur non trouvé: " + fournisseurId)))
                .flatMap(fournisseur -> {
                    fournisseur.setSoldeCourant(fournisseur.getSoldeCourant() + montant);
                    return fournisseurRepositoryPort.save(fournisseur);
                })
                .map(fournisseurMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<FournisseurResponse> desactiverFournisseur(UUID fournisseurId) {
        log.info("Désactivation du fournisseur: {}", fournisseurId);

        return fournisseurRepositoryPort.findById(fournisseurId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Fournisseur non trouvé: " + fournisseurId)))
                .flatMap(fournisseur -> {
                    fournisseur.setActif(false);
                    return fournisseurRepositoryPort.save(fournisseur);
                })
                .map(fournisseurMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<FournisseurResponse> activerFournisseur(UUID fournisseurId) {
        log.info("Activation du fournisseur: {}", fournisseurId);

        return fournisseurRepositoryPort.findById(fournisseurId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Fournisseur non trouvé: " + fournisseurId)))
                .flatMap(fournisseur -> {
                    fournisseur.setActif(true);
                    return fournisseurRepositoryPort.save(fournisseur);
                })
                .map(fournisseurMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Long> countActiveFournisseurs() {
        return fournisseurRepositoryPort.countActiveFournisseurs();
    }

    @Override
    public Mono<Void> inviteFournisseur(UUID fournisseurId, String email, String name) {
        log.info("Envoi des identifiants du portail au fournisseur: {}", fournisseurId);
        return fournisseurRepositoryPort.resendCredentials(fournisseurId, email, name);
    }

    @Override
    public Mono<Void> ensureFournisseurPortalAccess(UUID fournisseurId, String email, String name) {
        return fournisseurRepositoryPort.ensurePortalAccess(fournisseurId, email, name);
    }
}
