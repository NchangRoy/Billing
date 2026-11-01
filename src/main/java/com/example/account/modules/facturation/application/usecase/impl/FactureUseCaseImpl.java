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
import com.example.account.modules.facturation.model.enums.TypePaiementFacture;
import com.example.account.modules.facturation.service.DocPermissionService;
import com.example.account.modules.facturation.service.ExternalServices.ProductExternalService;
import com.example.account.modules.facturation.service.PdfGeneratorService;
import com.example.account.modules.facturation.service.EmailService;
import com.example.account.modules.notification.domain.port.input.LiveNotificationUseCase;
import com.example.account.modules.notification.model.entity.PendingInvoiceSummary;
import com.example.account.modules.paymentgateway.domain.port.output.PaymentGatewayPort;
import com.example.account.modules.paymentgateway.dto.enums.PaymentMethodType;
import com.example.account.modules.paymentgateway.dto.enums.PaymentProvider;
import com.example.account.modules.paymentgateway.dto.request.PaymentOrderInitiateRequest;
import com.example.account.modules.shared.dto.kernel.KernelApiResponse;
import com.example.account.modules.shared.dto.kernel.KernelOrganizationResponse;
import com.example.account.modules.tiers.domain.port.input.ClientUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    private final ClientUseCase clientUseCase;
    private final com.example.account.modules.tiers.domain.port.input.ThirdPartyContactUseCase thirdPartyContactUseCase;
    private final com.example.account.modules.facturation.service.FactureActorIndexService factureActorIndexService;
    private final LiveNotificationUseCase liveNotificationUseCase;
    private final PaymentGatewayPort paymentGatewayPort;
    @Qualifier("kernelWebClient")
    private final WebClient kernelWebClient;

    @Value("${client-portal.frontend-url}")
    private String portalFrontendUrl;

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
                    final UUID factureId = docId;
                    return recordActorIndex(savedFacture, factureId)
                            .then(enqueueLiveNotification(savedFacture, factureId))
                            .then(grantOwnerPermission(savedFacture.getCreatedBy(), factureId, savedFacture));
                });
    }

    /**
     * Facture isn't a local table (see FactureActorIndexEntity) — this
     * populates Billing's own small factureId->actorId index so the client
     * portal can find "my invoices" without a cross-service join.
     */
    private Mono<Void> recordActorIndex(FactureResponse savedFacture, UUID factureId) {
        if (factureId == null || savedFacture.getIdClient() == null) {
            return Mono.empty();
        }
        UUID idClient;
        try {
            idClient = UUID.fromString(savedFacture.getIdClient());
        } catch (IllegalArgumentException e) {
            return Mono.empty();
        }
        return thirdPartyContactUseCase.resolveActorId(idClient)
                .flatMap(actorId -> factureActorIndexService.record(factureId, actorId))
                .onErrorResume(e -> {
                    log.warn("Could not resolve actor id for client {} (continuing without it): {}", idClient, e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Best-effort: queues an invoice snapshot for the org's Telegram
     * live-notification rules. Snapshotting now (rather than re-fetching from
     * Kernel at flush time, when there's no caller token since it's a
     * background scheduler tick) is deliberate — see PendingInvoiceSummary.
     * Never fails invoice creation — a notification miss shouldn't roll back the facture.
     */
    private Mono<Void> enqueueLiveNotification(FactureResponse savedFacture, UUID factureId) {
        UUID organizationId = savedFacture.getOrganizationId();
        if (organizationId == null || factureId == null) return Mono.empty();
        return resolveOrganizationName(organizationId)
                .defaultIfEmpty("")
                .flatMap(orgName -> {
                    PendingInvoiceSummary invoice = PendingInvoiceSummary.builder()
                            .id(factureId)
                            .numeroFacture(savedFacture.getNumeroFacture())
                            .montant(savedFacture.getMontantTTC() != null ? savedFacture.getMontantTTC() : savedFacture.getMontantTotal())
                            .devise(savedFacture.getDevise())
                            .nomClient(savedFacture.getNomClient())
                            .organizationName(orgName.isBlank() ? null : orgName)
                            .build();
                    return liveNotificationUseCase.enqueueInvoice(organizationId, invoice);
                })
                .onErrorResume(e -> {
                    log.warn("Failed to enqueue live notification for facture {}: {}", factureId, e.getMessage());
                    return Mono.empty();
                });
    }

    private static final ParameterizedTypeReference<KernelApiResponse<List<KernelOrganizationResponse>>> KERNEL_ORG_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    /**
     * Resolved here (within the real authenticated request creating the
     * invoice) rather than at flush time — the live-notification scheduler
     * tick has no caller token, so a Kernel call there would always fail. See
     * PendingInvoiceSummary: the whole point of snapshotting is to capture
     * whatever needs a token now, while one exists.
     */
    private Mono<String> resolveOrganizationName(UUID organizationId) {
        return kernelWebClient.get()
                .uri("/api/organizations")
                .retrieve()
                .bodyToMono(KERNEL_ORG_LIST_TYPE)
                .mapNotNull(KernelApiResponse::getData)
                .flatMapMany(Flux::fromIterable)
                .filter(org -> organizationId.equals(org.getId()))
                .next()
                .map(org -> org.getDisplayName() != null ? org.getDisplayName()
                        : org.getShortName() != null ? org.getShortName() : org.getLegalName())
                .onErrorResume(e -> Mono.empty());
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

    /**
     * Marks the invoice ENVOYE, bootstraps the client's (login-based) portal
     * access only if they've never had it, then emails a "new document"
     * notification — same pattern as DevisUseCaseImpl.sendToPortal /
     * BonAchatService.sendToPortal. Only fires from BROUILLON: calling this
     * again on an already-sent (or paid/cancelled/etc) invoice is a no-op,
     * so double-submits don't re-provision access or re-send the email.
     * Status is only persisted once the email has actually gone out, so a
     * failure here never leaves the invoice falsely marked as sent.
     */
    @Override
    @Transactional
    public Mono<Void> sendToPortal(UUID factureId, com.example.account.modules.facturation.dto.request.ShareDocumentRequest request) {
        log.info("Envoi de la facture {} vers le portail client", factureId);
        return factureServicePort.findById(factureId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture non trouvée: " + factureId)))
                .flatMap(facture -> {
                    if (facture.getEtat() != StatutFacture.BROUILLON) {
                        log.info("Facture {} déjà envoyée (état: {}), envoi ignoré", factureId, facture.getEtat());
                        return Mono.empty();
                    }
                    // Lets the "Send to Contact" picker target any address-book
                    // contact instead of the facture's own stored emailClient —
                    // falls back to that stored email when no contact was picked.
                    String targetEmail = (request != null && request.getContactEmail() != null && !request.getContactEmail().isBlank())
                            ? request.getContactEmail() : facture.getEmailClient();
                    String targetName = (request != null && request.getContactName() != null && !request.getContactName().isBlank())
                            ? request.getContactName() : facture.getNomClient();
                    if (targetEmail == null || targetEmail.isBlank()) {
                        return Mono.error(new IllegalStateException("Le client n'a pas d'adresse email renseignée."));
                    }
                    return maybeInitiatePaymentRequest(facture)
                            .then(clientUseCase.ensureClientPortalAccess(parseUuid(facture.getIdClient()), targetEmail, targetName)
                                    // Kernel's ensure-portal-access endpoint isn't deployed yet (404) — this
                                    // is a bootstrap-only nicety anyway (grants login to brand-new clients),
                                    // so a failure here shouldn't block the actual notification email.
                                    .onErrorResume(e -> {
                                        log.warn("ensureClientPortalAccess failed for facture {} (continuing anyway): {}", factureId, e.getMessage());
                                        return Mono.empty();
                                    }))
                            .then(emailService.sendPortalDocumentNotification(
                                    targetEmail, targetName,
                                    "Facture", facture.getNumeroFacture(),
                                    portalFrontendUrl + "/portal/login"))
                            .then(Mono.defer(() -> factureServicePort.updateFacture(factureId, toSentUpdateRequest(facture))));
                })
                .then();
    }

    /**
     * When the invoice's chosen payment method is mobile money, initiate a real
     * Kernel payment order for the remaining balance and stash its id on the
     * (in-memory) facture — toSentUpdateRequest below persists whatever's on
     * it as referenceCommande — so the customer portal can offer a "Pay"
     * button that jumps straight to the hosted checkout. Non-blocking: a
     * failure here must never stop the invoice from actually being sent, same
     * tolerance already applied to ensureClientPortalAccess above.
     */
    private Mono<Void> maybeInitiatePaymentRequest(FactureResponse facture) {
        boolean isMobileMoney = facture.getModeReglement() == TypePaiementFacture.MOBILE_MONEY
                || facture.getModeReglement() == TypePaiementFacture.ORANGE_MONEY;
        if (!isMobileMoney || facture.getMontantRestant() == null || facture.getMontantRestant().signum() <= 0) {
            return Mono.empty();
        }
        PaymentOrderInitiateRequest request = PaymentOrderInitiateRequest.builder()
                .amount(facture.getMontantRestant())
                .currency(facture.getDevise())
                .provider(PaymentProvider.MYCOOLPAY)
                .method(PaymentMethodType.MOBILE_MONEY)
                .payerReference(facture.getTelephoneClient())
                .description("Facture " + facture.getNumeroFacture())
                .build();
        return paymentGatewayPort.initiateOrder(request)
                .doOnNext(order -> facture.setReferenceCommande(order.getId()))
                .onErrorResume(e -> {
                    log.warn("Payment order initiation failed for facture {} (continuing anyway): {}", facture.getIdFacture(), e.getMessage());
                    return Mono.empty();
                })
                .then();
    }

    private UUID parseUuid(String value) {
        try {
            return value != null ? UUID.fromString(value) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** sales-core has no dedicated "mark envoyé" endpoint (unlike /paye), so sending requires round-tripping the whole record through the generic update. */
    private FactureCreateRequest toSentUpdateRequest(FactureResponse facture) {
        return FactureCreateRequest.builder()
                .numeroFacture(facture.getNumeroFacture())
                .dateFacturation(facture.getDateFacturation())
                .dateEcheance(facture.getDateEcheance())
                .dateSysteme(facture.getDateSysteme())
                .type(facture.getType())
                .etat(StatutFacture.ENVOYE)
                .idClient(parseUuid(facture.getIdClient()))
                .nomClient(facture.getNomClient())
                .adresseClient(facture.getAdresseClient())
                .emailClient(facture.getEmailClient())
                .telephoneClient(facture.getTelephoneClient())
                .lignesFacture(toLigneCreateRequests(facture.getLignesFacture()))
                .montantHT(facture.getMontantHT())
                .montantTVA(facture.getMontantTVA())
                .montantTTC(facture.getMontantTTC())
                .montantTotal(facture.getMontantTotal())
                .finalAmount(facture.getFinalAmount())
                .montantRestant(facture.getMontantRestant())
                .applyVat(facture.getApplyVat())
                .devise(facture.getDevise())
                .tauxChange(facture.getTauxChange())
                .modeReglement(facture.getModeReglement())
                .conditionsPaiement(facture.getConditionsPaiement())
                .nbreEcheance(facture.getNbreEcheance())
                .nosRef(facture.getNosRef())
                .vosRef(facture.getVosRef())
                .referenceCommande(facture.getReferenceCommande())
                .idDevisOrigine(parseUuid(facture.getIdDevisOrigine()))
                .notes(facture.getNotes())
                .pdfPath(facture.getPdfPath())
                .envoyeParEmail(true)
                .dateEnvoiEmail(LocalDateTime.now())
                .remiseGlobalePourcentage(facture.getRemiseGlobalePourcentage())
                .remiseGlobaleMontant(facture.getRemiseGlobaleMontant())
                .referalClientId(facture.getReferalClientId())
                .organizationId(facture.getOrganizationId())
                .agencyId(facture.getAgencyId())
                .createdBy(facture.getCreatedBy())
                .originType(facture.getOriginType())
                .sessionId(facture.getSessionId())
                .build();
    }

    private java.util.List<com.example.account.modules.facturation.dto.request.LigneFactureCreateRequest> toLigneCreateRequests(
            java.util.List<com.example.account.modules.facturation.dto.response.LigneFactureResponse> lignes) {
        if (lignes == null) return null;
        return lignes.stream()
                .map(l -> com.example.account.modules.facturation.dto.request.LigneFactureCreateRequest.builder()
                        .quantite(l.getQuantite())
                        .description(l.getDescription())
                        .debit(l.getDebit())
                        .credit(l.getCredit())
                        .isTaxLine(l.getIsTaxLine())
                        .idProduit(l.getIdProduit())
                        .nomProduit(l.getNomProduit())
                        .prixUnitaire(l.getPrixUnitaire())
                        .montantTotal(l.getMontantTotal())
                        .build())
                .toList();
    }
}
