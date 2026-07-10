package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.port.input.FactureUseCase;
import com.example.account.modules.facturation.domain.port.output.AccountingServicePort;
import com.example.account.modules.facturation.domain.port.output.FactureEventPort;
import com.example.account.modules.facturation.domain.port.output.FactureServicePort;
import com.example.account.modules.facturation.domain.port.output.SellerServicePort;
import com.example.account.modules.facturation.dto.request.FactureCreateRequest;
import com.example.account.modules.facturation.dto.request.AssignDocPermissionRequest;
import com.example.account.modules.facturation.dto.response.FactureResponse;
import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;
import com.example.account.modules.facturation.model.enums.StatutFacture;
import com.example.account.modules.facturation.service.DocPermissionService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactureUseCaseImpl implements FactureUseCase {

    private final FactureServicePort factureServicePort;
    private final FactureEventPort factureEventPort;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;
    private final AccountingServicePort accountingService;
    private final SellerServicePort sellerService;
    private final ProductExternalService productExternalService;
    private final DocPermissionService docPermissionService;

    private <T> Mono<T> grantOwnerPermission(UUID sellerId, UUID docId, T response) {
        if (sellerId == null || docId == null) return Mono.just(response);
        AssignDocPermissionRequest request = new AssignDocPermissionRequest();
        request.setSellerId(sellerId);
        request.setDocId(docId);
        request.setDocType(DocType.FACTURE);
        request.setPermission(DocPermissionLevel.OWNER);
        return docPermissionService.grant(request)
                .thenReturn(response)
                .onErrorResume(e -> {
                    log.error("Failed to grant owner doc-permission for facture {}: {}", docId, e.getMessage());
                    return Mono.just(response);
                });
    }

    @Transactional
    public Mono<FactureResponse> createFacture(FactureCreateRequest request) {
        log.info("Création d'une nouvelle facture pour le client: {}", request.getIdClient());
        // Release reservations before creating the final invoice
        productExternalService.releaseProductsForSeller(request.getCreatedBy());
        return factureServicePort.createFacture(request)
                .flatMap(savedFacture -> {
                    factureEventPort.publishFactureCreated(savedFacture);
                    log.info("Facture créée avec succès: {}", savedFacture.getNumeroFacture());
                    UUID docId = null;
                    try {
                        if (savedFacture.getIdFacture() != null) docId = UUID.fromString(savedFacture.getIdFacture());
                    } catch (IllegalArgumentException ignored) {}
                    return grantOwnerPermission(savedFacture.getCreatedBy(), docId, savedFacture);
                });
    }

    @Override
    @Transactional
    public Mono<FactureResponse> updateFacture(UUID factureId, FactureCreateRequest request) {
        log.info("Mise à jour de la facture: {}", factureId);
        return factureServicePort.updateFacture(factureId, request)
                .doOnSuccess(response -> {
                    factureEventPort.publishFactureUpdated(response);
                    log.info("Facture mise à jour avec succès: {}", factureId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<FactureResponse> getFactureById(UUID factureId) {
        log.info("Récupération de la facture: {}", factureId);
        return factureServicePort.findById(factureId);
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
    public Mono<Void> markFactureAccounted(UUID factureId) {
        return accountingService.markFactureAccounted(factureId);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<FactureResponse> getFactureByNumero(String numeroFacture) {
        log.info("Récupération de la facture par numéro: {}", numeroFacture);
        return factureServicePort.findByNumeroFacture(numeroFacture);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getAllFactures() {
        log.info("Récupération de toutes les factures");
        return factureServicePort.getAllFactures();
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getAllFactures(Pageable pageable) {
        log.info("Récupération de toutes les factures avec pagination");
        return factureServicePort.getAllFactures(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesByClient(UUID clientId) {
        log.info("Récupération des factures du client: {}", clientId);
        return factureServicePort.findByIdClient(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesByEtat(StatutFacture etat) {
        log.info("Récupération des factures par état: {}", etat);
        return factureServicePort.findByEtat(etat);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesEnRetard() {
        log.info("Récupération des factures en retard");
        return factureServicePort.findOverdueFactures();
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesNonPayees() {
        log.info("Récupération des factures non payées");
        return factureServicePort.findUnpaidFactures();
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesByPeriode(LocalDate dateDebut, LocalDate dateFin) {
        log.info("Récupération des factures entre {} et {}", dateDebut, dateFin);
        return factureServicePort.findByDateFacturationBetween(dateDebut, dateFin);
    }

    @Override
    @Transactional
    public Mono<Void> deleteFacture(UUID factureId) {
        log.info("Suppression de la facture: {}", factureId);
        return factureServicePort.deleteFacture(factureId)
                .then(docPermissionService.deleteByDocIdAndDocType(factureId, DocType.FACTURE))
                .doOnSuccess(v -> {
                    factureEventPort.publishFactureDeleted(factureId);
                    log.info("Facture supprimée avec succès: {}", factureId);
                });
    }

    @Override
    @Transactional
    public Mono<FactureResponse> marquerCommePaye(UUID factureId) {
        log.info("Marquage de la facture {} comme payée", factureId);
        return factureServicePort.marquerCommePaye(factureId)
                .doOnSuccess(response -> {
                    factureEventPort.publishFacturePaid(response);
                    log.info("Facture marquée comme payée: {}", factureId);
                });
    }

    @Override
    @Transactional
    public Mono<FactureResponse> enregistrerPaiement(UUID factureId, BigDecimal montantPaye) {
        log.info("Enregistrement d'un paiement de {} pour la facture {}", montantPaye, factureId);
        return factureServicePort.enregistrerPaiement(factureId, montantPaye)
                .doOnSuccess(response -> {
                    if (response.getMontantRestant() != null
                            && response.getMontantRestant().compareTo(BigDecimal.ZERO) == 0) {
                        factureEventPort.publishFacturePaid(response);
                    }
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Long> countByEtat(StatutFacture etat) {
        return factureServicePort.countByEtat(etat);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesByOrganizationId(UUID organizationId) {
        log.info("Récupération des factures par organisation: {}", organizationId);
        return factureServicePort.findByOrganizationId(organizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesByAgencyId(UUID agencyId) {
        log.info("Récupération des factures par agence: {}", agencyId);
        return factureServicePort.findByAgencyId(agencyId);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<FactureResponse> getFacturesBySellerId(UUID sellerId) {
        log.info("Récupération des factures accessibles par le vendeur: {}", sellerId);
        return docPermissionService.findBySellerAndDocType(sellerId, DocType.FACTURE)
                .flatMap(permission -> factureServicePort.findById(permission.getDocId())
                        .map(response -> {
                            response.setDocPermission(docPermissionService.toResponse(permission));
                            return response;
                        })
                        .onErrorResume(e -> {
                            log.warn("Facture access skipped: document {} cannot be retrieved. Error: {}",
                                    permission.getDocId(), e.getMessage());
                            return Mono.empty();
                        }));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Map<String, Object>> getAccountingSaleFacture(UUID factureId) {
        log.info("Querying accounting gateway (sale) from sales-core for: {}", factureId);
        return factureServicePort.getAccountingSaleFacture(factureId);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Map<String, Object>> getAccountingPurchaseFacture(UUID factureId) {
        log.info("Querying accounting gateway (purchase) from sales-core for: {}", factureId);
        return factureServicePort.getAccountingPurchaseFacture(factureId);
    }
}
