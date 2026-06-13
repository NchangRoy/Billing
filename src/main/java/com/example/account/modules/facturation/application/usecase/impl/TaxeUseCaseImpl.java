package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.port.input.TaxeUseCase;
import com.example.account.modules.facturation.domain.port.output.TaxeRepositoryPort;
import com.example.account.modules.facturation.domain.port.output.TaxeEventPort;
import com.example.account.modules.facturation.dto.request.TaxeCreateRequest;
import com.example.account.modules.facturation.dto.request.TaxeUpdateRequest;
import com.example.account.modules.facturation.dto.response.TaxeResponse;
import com.example.account.modules.facturation.mapper.TaxeMapper;
import com.example.account.modules.facturation.domain.model.Taxes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxeUseCaseImpl implements TaxeUseCase {

    private final TaxeRepositoryPort taxeRepositoryPort;
    private final TaxeMapper taxeMapper;
    private final TaxeEventPort taxeEventPort;

    @Override
    @Transactional
    public Mono<TaxeResponse> createTaxe(TaxeCreateRequest request) {
        log.info("Création d'une nouvelle taxe: {}", request.getNomTaxe());

        return taxeRepositoryPort.existsByNomTaxe(request.getNomTaxe())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Une taxe avec ce nom existe déjà"));
                    }
                    Taxes taxe = taxeMapper.toEntity(request);
                    if (taxe.getIdTaxe() == null) {
                        taxe.setIdTaxe(UUID.randomUUID());
                    }
                    return taxeRepositoryPort.save(taxe);
                })
                .map(savedTaxe -> {
                    TaxeResponse response = taxeMapper.toResponse(savedTaxe);
                    taxeEventPort.publishTaxeCreated(response);
                    log.info("Taxe créée avec succès: {}", savedTaxe.getIdTaxe());
                    return response;
                });
    }

    @Override
    @Transactional
    public Mono<TaxeResponse> updateTaxe(UUID taxeId, TaxeUpdateRequest request) {
        log.info("Mise à jour de la taxe: {}", taxeId);

        return taxeRepositoryPort.findById(taxeId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Taxe non trouvée: " + taxeId)))
                .flatMap(taxe -> {
                    taxeMapper.updateEntityFromRequest(request, taxe);
                    return taxeRepositoryPort.save(taxe);
                })
                .map(updatedTaxe -> {
                    TaxeResponse response = taxeMapper.toResponse(updatedTaxe);
                    taxeEventPort.publishTaxeUpdated(response);
                    log.info("Taxe mise à jour avec succès: {}", taxeId);
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<TaxeResponse> getTaxeById(UUID taxeId) {
        log.info("Récupération de la taxe: {}", taxeId);
        return taxeRepositoryPort.findById(taxeId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Taxe non trouvée: " + taxeId)))
                .map(taxeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<TaxeResponse> getTaxeByNom(String nomTaxe) {
        log.info("Récupération de la taxe par nom: {}", nomTaxe);
        return taxeRepositoryPort.findByNomTaxe(nomTaxe)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Taxe non trouvée avec nom: " + nomTaxe)))
                .map(taxeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TaxeResponse> getAllTaxes() {
        log.info("Récupération de toutes les taxes");
        return taxeRepositoryPort.findAll()
                .map(taxeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TaxeResponse> getAllTaxes(Pageable pageable) {
        log.info("Récupération de toutes les taxes (paginée)");
        return taxeRepositoryPort.findAll()
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .map(taxeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TaxeResponse> getActiveTaxes() {
        log.info("Récupération des taxes actives");
        return taxeRepositoryPort.findAllActiveTaxes()
                .map(taxeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TaxeResponse> getTaxesByType(String typeTaxe) {
        log.info("Récupération des taxes par type: {}", typeTaxe);
        return taxeRepositoryPort.findByTypeTaxe(typeTaxe)
                .map(taxeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TaxeResponse> getActiveTaxesByType(String typeTaxe) {
        log.info("Récupération des taxes actives par type: {}", typeTaxe);
        return taxeRepositoryPort.findActiveByTypeTaxe(typeTaxe)
                .map(taxeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TaxeResponse> getTaxesByPorte(String porteTaxe) {
        log.info("Récupération des taxes par portée: {}", porteTaxe);
        return taxeRepositoryPort.findByPorteTaxe(porteTaxe)
                .map(taxeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TaxeResponse> getTaxesByPositionFiscale(String positionFiscale) {
        log.info("Récupération des taxes par position fiscale: {}", positionFiscale);
        return taxeRepositoryPort.findByPositionFiscale(positionFiscale)
                .map(taxeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TaxeResponse> getTaxesByCalculRange(BigDecimal minTaux, BigDecimal maxTaux) {
        log.info("Récupération des taxes par plage de calcul: {} - {}", minTaux, maxTaux);
        return taxeRepositoryPort.findByCalculTaxeBetween(minTaux, maxTaux)
                .map(taxeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TaxeResponse> getTaxesByMontantRange(BigDecimal minMontant, BigDecimal maxMontant) {
        log.info("Récupération des taxes par plage de montant: {} - {}", minMontant, maxMontant);
        return taxeRepositoryPort.findByMontantBetween(minMontant, maxMontant)
                .map(taxeMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> deleteTaxe(UUID taxeId) {
        log.info("Suppression de la taxe: {}", taxeId);
        return taxeRepositoryPort.existsById(taxeId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Taxe non trouvée: " + taxeId));
                    }
                    return taxeRepositoryPort.deleteById(taxeId)
                            .then(Mono.fromRunnable(() -> taxeEventPort.publishTaxeDeleted(taxeId)));
                });
    }

    @Override
    @Transactional
    public Mono<TaxeResponse> activerTaxe(UUID taxeId) {
        log.info("Activation de la taxe: {}", taxeId);
        return taxeRepositoryPort.findById(taxeId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Taxe non trouvée: " + taxeId)))
                .flatMap(taxe -> {
                    taxe.setActif(true);
                    return taxeRepositoryPort.save(taxe);
                })
                .map(updatedTaxe -> {
                    TaxeResponse response = taxeMapper.toResponse(updatedTaxe);
                    taxeEventPort.publishTaxeUpdated(response);
                    log.info("Taxe activée avec succès: {}", taxeId);
                    return response;
                });
    }

    @Override
    @Transactional
    public Mono<TaxeResponse> desactiverTaxe(UUID taxeId) {
        log.info("Désactivation de la taxe: {}", taxeId);
        return taxeRepositoryPort.findById(taxeId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Taxe non trouvée: " + taxeId)))
                .flatMap(taxe -> {
                    taxe.setActif(false);
                    return taxeRepositoryPort.save(taxe);
                })
                .map(updatedTaxe -> {
                    TaxeResponse response = taxeMapper.toResponse(updatedTaxe);
                    taxeEventPort.publishTaxeUpdated(response);
                    log.info("Taxe désactivée avec succès: {}", taxeId);
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Long> countActiveTaxes() {
        return taxeRepositoryPort.countActiveTaxes();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Long> countByType(String typeTaxe) {
        return taxeRepositoryPort.countByTypeTaxe(typeTaxe);
    }
}
