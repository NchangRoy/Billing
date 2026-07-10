package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.core.exception.SalesCoreErrorMapper;
import com.example.account.modules.facturation.domain.port.output.FactureFournisseurServicePort;
import com.example.account.modules.facturation.dto.request.FactureFournisseurCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureFournisseurResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Function;

@Service
public class FactureFournisseurServiceAdapter implements FactureFournisseurServicePort {

    private final WebClient salesCoreWebClient;

    public FactureFournisseurServiceAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient) {
        this.salesCoreWebClient = salesCoreWebClient;
    }

    private static final Function<ClientResponse, Mono<? extends Throwable>> SALES_CORE_ERROR =
            SalesCoreErrorMapper.forContext("sales-core facture fournisseur error");

    @Override
    public Mono<FactureFournisseurResponse> createFacture(FactureFournisseurCreateRequest request) {
        return salesCoreWebClient
                .post()
                .uri("/api/factures-fournisseur")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(FactureFournisseurResponse.class);
    }

    @Override
    public Mono<FactureFournisseurResponse> updateFacture(UUID id, FactureFournisseurCreateRequest request) {
        return salesCoreWebClient
                .put()
                .uri("/api/factures-fournisseur/{id}", id)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(FactureFournisseurResponse.class);
    }

    @Override
    public Mono<FactureFournisseurResponse> findById(UUID id) {
        return salesCoreWebClient
                .get()
                .uri("/api/factures-fournisseur/{id}", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(FactureFournisseurResponse.class);
    }

    @Override
    public Flux<FactureFournisseurResponse> getAllFactures() {
        return salesCoreWebClient
                .get()
                .uri("/api/factures-fournisseur")
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureFournisseurResponse.class);
    }

    @Override
    public Flux<FactureFournisseurResponse> getByOrganizationId(UUID organizationId) {
        return salesCoreWebClient
                .get()
                .uri("/api/factures-fournisseur/organisation/{organizationId}", organizationId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureFournisseurResponse.class);
    }

    @Override
    public Flux<FactureFournisseurResponse> getByAgencyId(UUID agencyId) {
        return salesCoreWebClient
                .get()
                .uri("/api/factures-fournisseur/agence/{agencyId}", agencyId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureFournisseurResponse.class);
    }

    @Override
    public Mono<Void> deleteFacture(UUID id) {
        return salesCoreWebClient
                .delete()
                .uri("/api/factures-fournisseur/{id}", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(Void.class);
    }
}
