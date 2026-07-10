package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.core.exception.SalesCoreErrorMapper;
import com.example.account.modules.facturation.domain.port.output.FactureServicePort;
import com.example.account.modules.facturation.dto.request.FactureCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureResponse;
import com.example.account.modules.facturation.model.enums.StatutFacture;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Delegates all Facture (client invoice) persistence to sales-core via WebClient.
 * account itself no longer owns a local factures table after Phase 2 migration.
 */
@Service
public class FactureServiceAdapter implements FactureServicePort {

    private final WebClient salesCoreWebClient;

    public FactureServiceAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient) {
        this.salesCoreWebClient = salesCoreWebClient;
    }

    private static final Function<ClientResponse, Mono<? extends Throwable>> SALES_CORE_ERROR =
            SalesCoreErrorMapper.forContext("sales-core facture error");

    @Override
    public Mono<FactureResponse> createFacture(FactureCreateRequest request) {
        return salesCoreWebClient.post()
                .uri("/api/factures")
                .bodyValue(request)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(FactureResponse.class);
    }

    @Override
    public Mono<FactureResponse> updateFacture(UUID factureId, FactureCreateRequest request) {
        return salesCoreWebClient.put()
                .uri("/api/factures/{id}", factureId)
                .bodyValue(request)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(FactureResponse.class);
    }

    @Override
    public Mono<FactureResponse> findById(UUID factureId) {
        return salesCoreWebClient.get()
                .uri("/api/factures/{id}", factureId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(FactureResponse.class);
    }

    @Override
    public Mono<FactureResponse> findByNumeroFacture(String numeroFacture) {
        return salesCoreWebClient.get()
                .uri("/api/factures/numero/{numero}", numeroFacture)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(FactureResponse.class);
    }

    @Override
    public Flux<FactureResponse> getAllFactures() {
        return salesCoreWebClient.get()
                .uri("/api/factures")
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class);
    }

    @Override
    public Flux<FactureResponse> getAllFactures(Pageable pageable) {
        // sales-core returns the full list; account handles pagination client-side (same behaviour as before)
        return getAllFactures()
                .skip(pageable.getOffset())
                .take(pageable.getPageSize());
    }

    @Override
    public Flux<FactureResponse> findByIdClient(UUID clientId) {
        return salesCoreWebClient.get()
                .uri("/api/factures/client/{clientId}", clientId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class);
    }

    @Override
    public Flux<FactureResponse> findByEtat(StatutFacture etat) {
        return salesCoreWebClient.get()
                .uri("/api/factures/etat/{etat}", etat.name())
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class);
    }

    @Override
    public Flux<FactureResponse> findOverdueFactures() {
        return salesCoreWebClient.get()
                .uri("/api/factures/en-retard")
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class);
    }

    @Override
    public Flux<FactureResponse> findUnpaidFactures() {
        return salesCoreWebClient.get()
                .uri("/api/factures/non-payees")
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class);
    }

    @Override
    public Flux<FactureResponse> findByDateFacturationBetween(LocalDate dateDebut, LocalDate dateFin) {
        // sales-core has this endpoint via its own repository; delegate via query params
        return salesCoreWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/factures")
                        .queryParam("dateDebut", dateDebut.toString())
                        .queryParam("dateFin", dateFin.toString())
                        .build())
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class);
    }

    @Override
    public Mono<Void> deleteFacture(UUID factureId) {
        return salesCoreWebClient.delete()
                .uri("/api/factures/{id}", factureId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<FactureResponse> marquerCommePaye(UUID factureId) {
        return salesCoreWebClient.put()
                .uri("/api/factures/{id}/paye", factureId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(FactureResponse.class);
    }

    @Override
    public Mono<FactureResponse> enregistrerPaiement(UUID factureId, BigDecimal montantPaye) {
        return salesCoreWebClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/factures/{id}/paiement")
                        .queryParam("montant", montantPaye.toString())
                        .build(factureId))
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(FactureResponse.class);
    }

    @Override
    public Mono<Long> countByEtat(StatutFacture etat) {
        return salesCoreWebClient.get()
                .uri("/api/factures/etat/{etat}", etat.name())
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .count();
    }

    @Override
    public Flux<FactureResponse> findByOrganizationId(UUID organizationId) {
        return salesCoreWebClient.get()
                .uri("/api/factures/organisation/{organizationId}", organizationId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class);
    }

    @Override
    public Flux<FactureResponse> findByAgencyId(UUID agencyId) {
        return salesCoreWebClient.get()
                .uri("/api/factures/agence/{agencyId}", agencyId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class);
    }

    @Override
    public Flux<FactureResponse> findByCreatedBy(UUID sellerId) {
        return salesCoreWebClient.get()
                .uri("/api/factures/vendeur/{sellerId}", sellerId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class);
    }

    @Override
    public Mono<Map<String, Object>> getAccountingSaleFacture(UUID factureId) {
        return salesCoreWebClient.get()
                .uri("/api/factures/sales/{id}", factureId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    @Override
    public Mono<Map<String, Object>> getAccountingPurchaseFacture(UUID factureId) {
        return salesCoreWebClient.get()
                .uri("/api/factures/purchases/{id}", factureId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
