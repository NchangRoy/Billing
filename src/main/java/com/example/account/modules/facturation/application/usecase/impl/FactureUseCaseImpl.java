package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.model.Facture;
import com.example.account.modules.facturation.domain.model.LigneFacture;
import com.example.account.modules.facturation.domain.port.input.FactureUseCase;
import com.example.account.modules.facturation.domain.port.output.AccountingServicePort;
import com.example.account.modules.facturation.domain.port.output.FactureEventPort;
import com.example.account.modules.facturation.domain.port.output.FactureRepositoryPort;
import com.example.account.modules.facturation.domain.port.output.SellerServicePort;
import com.example.account.modules.facturation.dto.request.FactureCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import com.example.account.modules.facturation.mapper.FactureMapper;
import com.example.account.modules.facturation.model.enums.StatutFacture;
import com.example.account.modules.facturation.service.ExternalServices.ProductExternalService;
import com.example.account.modules.facturation.service.PdfGeneratorService;
import com.example.account.modules.facturation.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactureUseCaseImpl implements FactureUseCase {

    private final FactureRepositoryPort factureRepository;
    private final FactureMapper factureMapper;
    private final FactureEventPort factureEventPort;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;
    private final AccountingServicePort accountingService;
    private final SellerServicePort sellerService;
    private final ProductExternalService productExternalService;

    @Transactional
    public Mono<FactureResponse> createFacture(FactureCreateRequest request) {
        log.info("Création d'une nouvelle facture pour le client: {}", request.getIdClient());

        Facture facture = factureMapper.toEntity(request);
        if (facture.getIdFacture() == null) {
            facture.setIdFacture(UUID.randomUUID());
        }

        // Compute totals from lines if null or zero
        if (facture.getMontantTotal() == null || facture.getMontantTotal().compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal total = BigDecimal.ZERO;
            if (facture.getLignesFacture() != null) {
                for (LigneFacture line : facture.getLignesFacture()) {
                    if (line.getMontantTotal() != null) {
                        total = total.add(line.getMontantTotal());
                    } else if (line.getPrixUnitaire() != null && line.getQuantite() != null) {
                        total = total.add(line.getPrixUnitaire().multiply(BigDecimal.valueOf(line.getQuantite())));
                    }
                }
            }
            facture.setMontantTotal(total);
            if (facture.getMontantHT() == null) {
                facture.setMontantHT(total);
            }
            if (facture.getMontantTTC() == null) {
                facture.setMontantTTC(total);
            }
        }

        // Set default remaining amount if null or zero
        if (facture.getMontantRestant() == null || facture.getMontantRestant().compareTo(BigDecimal.ZERO) == 0) {
            facture.setMontantRestant(facture.getMontantTotal());
        }

        //delete the reservations made by the creator;
        productExternalService.releaseProductsForSeller(facture.getCreatedBy());
        //save facture
        return factureRepository.insert(facture)
                .map(savedFacture -> {
                    FactureResponse response = factureMapper.toResponse(savedFacture);
                    factureEventPort.publishFactureCreated(response);
                    log.info("Facture créée avec succès: {}", savedFacture.getNumeroFacture());
                    return response;
                });
    }

    @Override
    @Transactional
    public Mono<FactureResponse> updateFacture(UUID factureId, FactureCreateRequest request) {
        log.info("Mise à jour de la facture: {}", factureId);

        return factureRepository.findById(factureId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture non trouvée: " + factureId)))
                .flatMap(facture -> {
                    factureMapper.updateEntityFromRequest(request, facture);
                    return factureRepository.save(facture);
                })
                .map(updatedFacture -> {
                    FactureResponse response = factureMapper.toResponse(updatedFacture);
                    factureEventPort.publishFactureUpdated(response);
                    log.info("Facture mise à jour avec succès: {}", factureId);
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<FactureResponse> getFactureById(UUID factureId) {
        log.info("Récupération de la facture: {}", factureId);

        return factureRepository.findById(factureId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture non trouvée: " + factureId)))
                .map(factureMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Void> accountFacture(UUID factureId) {
        return accountingService.sendFactureData(factureId)
                .onErrorResume(e -> {
                    log.error("Failed to sync facture {} with accounting: {}", factureId, e.getMessage());
                    return Mono.error(new Exception("Accounting sync failed: " + e.getMessage()));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<FactureResponse> getFactureByNumero(String numeroFacture) {
        log.info("Récupération de la facture par numéro: {}", numeroFacture);

        return factureRepository.findByNumeroFacture(numeroFacture)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture non trouvée avec numéro: " + numeroFacture)))
                .map(factureMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getAllFactures() {
        log.info("Récupération de toutes les factures");
        return factureRepository.findAll()
                .map(factureMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getAllFactures(Pageable pageable) {
        log.info("Récupération de toutes les factures avec pagination");
        return factureRepository.findAll()
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .map(factureMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesByClient(UUID clientId) {
        log.info("Récupération des factures du client: {}", clientId);
        return factureRepository.findByIdClient(clientId)
                .map(factureMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesByEtat(StatutFacture etat) {
        log.info("Récupération des factures par état: {}", etat);
        return factureRepository.findByEtat(etat)
                .map(factureMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesEnRetard() {
        log.info("Récupération des factures en retard");
        return factureRepository.findOverdueFactures(LocalDate.now())
                .map(factureMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesNonPayees() {
        log.info("Récupération des factures non payées");
        return factureRepository.findUnpaidFactures()
                .map(factureMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        log.info("Récupération des factures entre {} et {}", dateDebut, dateFin);
        return factureRepository.findByDateFacturationBetween(dateDebut, dateFin)
                .map(factureMapper::toResponse);
    }

    @Override
    @Transactional
    public Mono<Void> deleteFacture(UUID factureId) {
        log.info("Suppression de la facture: {}", factureId);

        return factureRepository.existsById(factureId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Facture non trouvée: " + factureId));
                    }
                    return factureRepository.deleteById(factureId)
                            .then(Mono.fromRunnable(() -> factureEventPort.publishFactureDeleted(factureId)));
                })
                .then()
                .doOnSuccess(v -> log.info("Facture supprimée avec succès: {}", factureId));
    }

    @Override
    @Transactional
    public Mono<FactureResponse> marquerCommePaye(UUID factureId) {
        log.info("Marquage de la facture {} comme payée", factureId);

        return factureRepository.findById(factureId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture non trouvée: " + factureId)))
                .flatMap(facture -> {
                    facture.setEtat(StatutFacture.PAYE);
                    facture.setMontantRestant(BigDecimal.ZERO);
                    return factureRepository.save(facture);
                })
                .map(updatedFacture -> {
                    FactureResponse response = factureMapper.toResponse(updatedFacture);
                    factureEventPort.publishFacturePaid(response);
                    log.info("Facture marquée comme payée: {}", factureId);
                    return response;
                });
    }

    @Override
    @Transactional
    public Mono<FactureResponse> enregistrerPaiement(UUID factureId, BigDecimal montantPaye) {
        log.info("Enregistrement d'un paiement de {} pour la facture {}", montantPaye, factureId);

        return factureRepository.findById(factureId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture non trouvée: " + factureId)))
                .flatMap(facture -> {
                    BigDecimal restant = facture.getMontantRestant();
                    if (restant == null) {
                        restant = facture.getMontantTotal() != null ? facture.getMontantTotal() :
                                  (facture.getMontantTTC() != null ? facture.getMontantTTC() : BigDecimal.ZERO);
                    }
                    BigDecimal nouveauMontantRestant = restant.subtract(montantPaye);

                    if (nouveauMontantRestant.compareTo(BigDecimal.ZERO) < 0) {
                        return Mono.error(new IllegalArgumentException("Le montant payé dépasse le montant restant"));
                    }

                    facture.setMontantRestant(nouveauMontantRestant);

                    if (nouveauMontantRestant.compareTo(BigDecimal.ZERO) == 0) {
                        facture.setEtat(StatutFacture.PAYE);
                    } else {
                        facture.setEtat(StatutFacture.PARTIELLEMENT_PAYE);
                    }

                    return factureRepository.save(facture)
                            .map(updatedFacture -> {
                                FactureResponse response = factureMapper.toResponse(updatedFacture);
                                if (nouveauMontantRestant.compareTo(BigDecimal.ZERO) == 0) {
                                    factureEventPort.publishFacturePaid(response);
                                }
                                return response;
                            });
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Long> countByEtat(StatutFacture etat) {
        return factureRepository.countByEtat(etat);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesByOrganizationId(UUID organizationId) {
        log.info("Récupération des factures par organisation: {}", organizationId);
        return factureRepository.findByOrganizationId(organizationId).map(factureMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesByAgencyId(UUID agencyId) {
        log.info("Récupération des factures par agence: {}", agencyId);
        return factureRepository.findByAgencyId(agencyId).map(factureMapper::toResponse);
    }
}
