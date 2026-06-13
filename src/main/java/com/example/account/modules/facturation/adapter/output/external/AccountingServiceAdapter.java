package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.facturation.domain.port.output.AccountingServicePort;
import com.example.account.modules.facturation.domain.port.output.FactureRepositoryPort;
import com.example.account.modules.facturation.domain.model.Facture;
import com.example.account.modules.facturation.dto.request.CreateInvoiceAccountingRequest;
import com.example.account.modules.core.context.ReactiveOrganizationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountingServiceAdapter implements AccountingServicePort {

    private final WebClient.Builder webClientBuilder;
    private final FactureRepositoryPort factureRepositoryPort;

    @Value("${comops.kernel.ip}")
    private String kernelIp;

    @Override
    public Mono<Void> sendFactureData(UUID factureId) {
        String url = String.format("http://%s/api/accounting/invoices/sale", kernelIp);

        return factureRepositoryPort.findById(factureId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture does not exist")))
                .flatMap(facture -> {
                    Mono<UUID> orgIdMono = ReactiveOrganizationContext.getOrganizationId()
                            .onErrorResume(e -> facture.getOrganizationId() != null ? 
                                    Mono.just(facture.getOrganizationId()) : 
                                    Mono.error(e));

                    return orgIdMono.flatMap(orgId -> {
                        CreateInvoiceAccountingRequest requestBody = new CreateInvoiceAccountingRequest(facture.getIdFacture(), "PENDING");
                        return webClientBuilder.build()
                                .post()
                                .uri(url)
                                .header("X-Tenant-ID", orgId.toString())
                                .bodyValue(requestBody)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, response -> 
                                    response.bodyToMono(String.class)
                                            .flatMap(errorBody -> Mono.error(new Exception("External service communication failed: " + errorBody))))
                                .bodyToMono(Void.class)
                                .doOnSuccess(res -> log.info("Facture sent successfully to accounting!"))
                                .then();
                    });
                })
                .onErrorMap(e -> new Exception("Accounting sync failed: " + e.getMessage()));
    }
}
