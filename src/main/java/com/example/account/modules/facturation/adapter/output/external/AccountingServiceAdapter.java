package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.facturation.domain.port.output.AccountingServicePort;
import com.example.account.modules.facturation.domain.port.output.FactureFournisseurServicePort;
import com.example.account.modules.facturation.domain.port.output.FactureRepositoryPort;
import com.example.account.modules.facturation.dto.request.CreateInvoiceAccountingRequest;
import com.example.account.modules.facturation.dto.request.FactureFournisseurCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureFournisseurResponse;
import com.example.account.modules.facturation.model.enums.StatutFacture;
import com.example.account.modules.facturation.model.enums.StatutFactureFournisseur;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Pure redirection to the external accounting backend — no accounting logic
 * lives here, just: forward the sync request, and locally flip the facture's
 * status so the frontend can show it's in flight (ACCOUNT_PENDING) or done
 * (ACCOUNTED, set via the callback methods once the accounting backend calls
 * back through sales-core's forwarding endpoints).
 */
@Service
@Slf4j
public class AccountingServiceAdapter implements AccountingServicePort {

    private final WebClient.Builder webClientBuilder;
    private final FactureRepositoryPort factureRepositoryPort;
    private final FactureFournisseurServicePort factureFournisseurServicePort;
    private final AccountingKernelAuthService accountingKernelAuthService;

    @Value("${comops.kernel.base-url}")
    private String kernelBaseUrl;

    public AccountingServiceAdapter(
            WebClient.Builder webClientBuilder,
            FactureRepositoryPort factureRepositoryPort,
            FactureFournisseurServicePort factureFournisseurServicePort,
            AccountingKernelAuthService accountingKernelAuthService) {
        this.webClientBuilder = webClientBuilder;
        this.factureRepositoryPort = factureRepositoryPort;
        this.factureFournisseurServicePort = factureFournisseurServicePort;
        this.accountingKernelAuthService = accountingKernelAuthService;
    }

    private static FactureFournisseurCreateRequest toUpdateRequest(FactureFournisseurResponse r) {
        return FactureFournisseurCreateRequest.builder()
                .numeroFacture(r.getNumeroFacture())
                .idFournisseur(r.getIdFournisseur())
                .nomFournisseur(r.getNomFournisseur())
                .adresseFournisseur(r.getAdresseFournisseur())
                .emailFournisseur(r.getEmailFournisseur())
                .telephoneFournisseur(r.getTelephoneFournisseur())
                .lines(r.getLines())
                .montantHT(r.getMontantHT())
                .montantTVA(r.getMontantTVA())
                .montantTTC(r.getMontantTTC())
                .montantTotal(r.getMontantTotal())
                .modeReglement(r.getModeReglement())
                .nbreEcheance(r.getNbreEcheance())
                .montantRestant(r.getMontantRestant())
                .dateFacture(r.getDateFacture())
                .dateEcheance(r.getDateEcheance())
                .statut(r.getStatut())
                .applyVat(r.getApplyVat())
                .devise(r.getDevise())
                .notes(r.getNotes())
                .pdfPath(r.getPdfPath())
                .createdBy(r.getCreatedBy())
                .idBonReception(r.getIdBonReception())
                .numeroBonReception(r.getNumeroBonReception())
                .dateSysteme(r.getDateSysteme())
                .organizationId(r.getOrganizationId())
                .agencyId(r.getAgencyId())
                .build();
    }

    @Override
    public Mono<Void> sendFactureData(UUID factureId) {
        String url = String.format("%s/api/accounting/invoices/sale", kernelBaseUrl);

        return factureRepositoryPort.findById(factureId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture does not exist")))
                .flatMap(facture -> {
                    Mono<UUID> orgIdMono = ReactiveOrganizationContext.getOrganizationId()
                            .onErrorResume(e -> facture.getOrganizationId() != null
                                    ? Mono.just(facture.getOrganizationId())
                                    : Mono.error(e));

                    return orgIdMono.flatMap(orgId ->
                            accountingKernelAuthService.getValidToken()
                                    .flatMap(token -> {
                                        CreateInvoiceAccountingRequest body =
                                                new CreateInvoiceAccountingRequest(facture.getIdFacture(), "PENDING");

                                        WebClient.RequestBodySpec req = webClientBuilder.build().post()
                                                .uri(url)
                                                .header("Authorization", "Bearer " + token)
                                                .header("X-Organization-Id", orgId.toString());

                                        return req.bodyValue(body)
                                                .retrieve()
                                                .onStatus(HttpStatusCode::isError, response ->
                                                        response.bodyToMono(String.class)
                                                                .flatMap(err -> Mono.error(
                                                                        new Exception("Accounting sync failed: " + err))))
                                                .bodyToMono(Void.class)
                                                .doOnSuccess(v -> log.info("Facture {} sent to accounting", factureId))
                                                .then();
                                    })
                    ).then(Mono.defer(() -> {
                        facture.setEtat(StatutFacture.ACCOUNT_PENDING);
                        return factureRepositoryPort.save(facture).then();
                    }));
                })
                .onErrorMap(e -> new Exception("Accounting sync failed: " + e.getMessage()));
    }

    @Override
    public Mono<Void> sendFactureFournisseurData(UUID factureFournisseurId) {
        String url = String.format("%s/api/accounting/invoices/purchase", kernelBaseUrl);

        return factureFournisseurServicePort.findById(factureFournisseurId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture fournisseur does not exist")))
                .flatMap(facture -> {
                    Mono<UUID> orgIdMono = ReactiveOrganizationContext.getOrganizationId()
                            .onErrorResume(e -> facture.getOrganizationId() != null
                                    ? Mono.just(facture.getOrganizationId())
                                    : Mono.error(e));

                    return orgIdMono.flatMap(orgId ->
                            accountingKernelAuthService.getValidToken()
                                    .flatMap(token -> {
                                        CreateInvoiceAccountingRequest body =
                                                new CreateInvoiceAccountingRequest(facture.getIdFactureFournisseur(), "PENDING");

                                        WebClient.RequestBodySpec req = webClientBuilder.build().post()
                                                .uri(url)
                                                .header("Authorization", "Bearer " + token)
                                                .header("X-Organization-Id", orgId.toString());

                                        return req.bodyValue(body)
                                                .retrieve()
                                                .onStatus(HttpStatusCode::isError, response ->
                                                        response.bodyToMono(String.class)
                                                                .flatMap(err -> Mono.error(
                                                                        new Exception("Accounting sync failed: " + err))))
                                                .bodyToMono(Void.class)
                                                .doOnSuccess(v -> log.info("Facture fournisseur {} sent to accounting", factureFournisseurId))
                                                .then();
                                    })
                    ).then(Mono.defer(() -> {
                        FactureFournisseurCreateRequest update = toUpdateRequest(facture);
                        update.setStatut(StatutFactureFournisseur.ACCOUNT_PENDING);
                        return factureFournisseurServicePort.updateFacture(factureFournisseurId, update).then();
                    }));
                })
                .onErrorMap(e -> new Exception("Accounting sync failed: " + e.getMessage()));
    }

    @Override
    public Mono<Void> markFactureAccounted(UUID factureId) {
        return factureRepositoryPort.findById(factureId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture does not exist: " + factureId)))
                .flatMap(facture -> {
                    facture.setEtat(StatutFacture.ACCOUNTED);
                    return factureRepositoryPort.save(facture);
                })
                .then();
    }

    @Override
    public Mono<Void> markFactureFournisseurAccounted(UUID factureFournisseurId) {
        return factureFournisseurServicePort.findById(factureFournisseurId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture fournisseur does not exist: " + factureFournisseurId)))
                .flatMap(facture -> {
                    FactureFournisseurCreateRequest update = toUpdateRequest(facture);
                    update.setStatut(StatutFactureFournisseur.ACCOUNTED);
                    return factureFournisseurServicePort.updateFacture(factureFournisseurId, update);
                })
                .then();
    }
}
