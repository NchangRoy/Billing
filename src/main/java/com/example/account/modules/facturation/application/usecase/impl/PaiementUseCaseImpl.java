package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.port.input.PaiementUseCase;
import com.example.account.modules.facturation.domain.port.output.PaiementRepositoryPort;
import com.example.account.modules.facturation.domain.port.output.PaiementEventPort;
import com.example.account.modules.facturation.dto.request.PaiementCreateRequest;
import com.example.account.modules.facturation.dto.request.PaiementUpdateRequest;
import com.example.account.modules.facturation.dto.response.PaiementResponse;
import com.example.account.modules.facturation.mapper.PaiementMapper;
import com.example.account.modules.facturation.domain.model.Paiement;
import com.example.account.modules.facturation.model.enums.TypePaiement;
import com.example.account.modules.facturation.domain.port.input.FactureUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaiementUseCaseImpl implements PaiementUseCase {

    private final PaiementRepositoryPort paiementRepositoryPort;
    private final PaiementMapper paiementMapper;
    private final PaiementEventPort paiementEventPort;
    private final FactureUseCase factureService;

    @Override
    @Transactional
    public Mono<PaiementResponse> createPaiement(PaiementCreateRequest request) {
        log.info("Création d'un nouveau paiement pour le client: {}", request.getIdClient());

        Paiement paiement = paiementMapper.toEntity(request);
        if (paiement.getIdPaiement() == null) {
            paiement.setIdPaiement(UUID.randomUUID());
        }

        return paiementRepositoryPort.save(paiement)
                .flatMap(savedPaiement -> {
                    Mono<Void> updateFactureMono = Mono.empty();
                    if (request.getIdFacture() != null) {
                        updateFactureMono = factureService.enregistrerPaiement(request.getIdFacture(), request.getMontant())
                                .then()
                                .onErrorResume(e -> {
                                    log.error("Erreur lors de la mise à jour de la facture: {}", e.getMessage());
                                    return Mono.empty();
                                });
                    }
                    return updateFactureMono.then(Mono.just(savedPaiement));
                })
                .map(savedPaiement -> {
                    PaiementResponse response = paiementMapper.toResponse(savedPaiement);
                    paiementEventPort.publishPaiementCreated(response);
                    log.info("Paiement créé avec succès: {}", savedPaiement.getIdPaiement());
                    return response;
                });
    }

    @Override
    @Transactional
    public Mono<PaiementResponse> updatePaiement(UUID paiementId, PaiementUpdateRequest request) {
        log.info("Mise à jour du paiement: {}", paiementId);

        return paiementRepositoryPort.findById(paiementId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Paiement non trouvé: " + paiementId)))
                .flatMap(paiement -> {
                    paiementMapper.updateEntityFromRequest(request, paiement);
                    return paiementRepositoryPort.save(paiement);
                })
                .map(updatedPaiement -> {
                    PaiementResponse response = paiementMapper.toResponse(updatedPaiement);
                    paiementEventPort.publishPaiementUpdated(response);
                    log.info("Paiement mis à jour avec succès: {}", paiementId);
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<PaiementResponse> getPaiementById(UUID paiementId) {
        log.info("Récupération du paiement: {}", paiementId);
        return paiementRepositoryPort.findById(paiementId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Paiement non trouvé: " + paiementId)))
                .map(paiementMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<PaiementResponse> getAllPaiements() {
        log.info("Récupération de tous les paiements");
        return paiementRepositoryPort.findAll()
                .map(paiementMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<PaiementResponse> getAllPaiements(Pageable pageable) {
        log.info("Récupération de tous les paiements avec pagination");
        return paiementRepositoryPort.findAll()
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .map(paiementMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<PaiementResponse> getPaiementsByClient(UUID clientId) {
        log.info("Récupération des paiements du client: {}", clientId);
        return paiementRepositoryPort.findByIdClient(clientId)
                .map(paiementMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<PaiementResponse> getPaiementsByFacture(UUID factureId) {
        log.info("Récupération des paiements de la facture: {}", factureId);
        return paiementRepositoryPort.findByIdFacture(factureId)
                .map(paiementMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<PaiementResponse> getPaiementsByModePaiement(TypePaiement modePaiement) {
        log.info("Récupération des paiements par mode de paiement: {}", modePaiement);
        return paiementRepositoryPort.findByModePaiement(modePaiement)
                .map(paiementMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<PaiementResponse> getPaiementsByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        log.info("Récupération des paiements entre {} et {}", dateDebut, dateFin);
        return paiementRepositoryPort.findByDateBetween(dateDebut, dateFin)
                .map(paiementMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> deletePaiement(UUID paiementId) {
        log.info("Suppression du paiement: {}", paiementId);
        return paiementRepositoryPort.existsById(paiementId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Paiement non trouvé: " + paiementId));
                    }
                    return paiementRepositoryPort.deleteById(paiementId)
                            .then(Mono.fromRunnable(() -> paiementEventPort.publishPaiementDeleted(paiementId)));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BigDecimal> getTotalPaiementsByClient(UUID clientId) {
        log.info("Calcul du total des paiements du client: {}", clientId);
        return paiementRepositoryPort.sumMontantByClient(clientId)
                .defaultIfEmpty(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BigDecimal> getTotalPaiementsByFacture(UUID factureId) {
        log.info("Calcul du total des paiements de la facture: {}", factureId);
        return paiementRepositoryPort.sumMontantByFacture(factureId)
                .defaultIfEmpty(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BigDecimal> getTotalPaiementsByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        log.info("Calcul du total des paiements entre {} et {}", dateDebut, dateFin);
        return paiementRepositoryPort.sumMontantByDateBetween(dateDebut, dateFin)
                .defaultIfEmpty(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Long> countPaiementsByClient(UUID clientId) {
        return paiementRepositoryPort.countByIdClient(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Long> countPaiementsByModePaiement(TypePaiement modePaiement) {
        return paiementRepositoryPort.countByModePaiement(modePaiement);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<PaiementResponse> getPaiementsByOrganizationId(UUID organizationId) {
        log.info("Récupération des paiements par organisation: {}", organizationId);
        return paiementRepositoryPort.findByOrganizationId(organizationId).map(paiementMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<PaiementResponse> getPaiementsByAgencyId(UUID agencyId) {
        log.info("Récupération des paiements par agence: {}", agencyId);
        return paiementRepositoryPort.findByAgencyId(agencyId).map(paiementMapper::toResponse);
    }
}
