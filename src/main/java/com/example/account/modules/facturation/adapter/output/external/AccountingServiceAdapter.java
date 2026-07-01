package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.facturation.domain.port.output.AccountingServicePort;
import com.example.account.modules.facturation.domain.port.output.FactureRepositoryPort;
import com.example.account.modules.facturation.dto.request.CreateInvoiceAccountingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class AccountingServiceAdapter implements AccountingServicePort {

    private final WebClient kernelWebClient;
    private final FactureRepositoryPort factureRepositoryPort;

    @Value("${comops.accounting_back.ip:10.205.243.11:8081}")
    private String accountingIp;

    public AccountingServiceAdapter(
            @Qualifier("kernelWebClient") WebClient kernelWebClient,
            FactureRepositoryPort factureRepositoryPort) {
        this.kernelWebClient = kernelWebClient;
        this.factureRepositoryPort = factureRepositoryPort;
    }

    @Override
    public Mono<Void> sendFactureData(UUID factureId) {
        String url = String.format("http://%s/api/accounting/invoices/sale", accountingIp);

        return factureRepositoryPort.findById(factureId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Facture does not exist")))
                .flatMap(facture -> {
                    Mono<UUID> orgIdMono = ReactiveOrganizationContext.getOrganizationId()
                            .onErrorResume(e -> facture.getOrganizationId() != null
                                    ? Mono.just(facture.getOrganizationId())
                                    : Mono.error(e));

                    return orgIdMono.flatMap(orgId ->
                            ReactiveOrganizationContext.getBearerToken()
                                    .defaultIfEmpty("")
                                    .flatMap(token -> {
                                        CreateInvoiceAccountingRequest body =
                                                new CreateInvoiceAccountingRequest(facture.getIdFacture(), "PENDING");

                                        WebClient.RequestBodySpec req = kernelWebClient.post()
                                                .uri(url)
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
                    );
                })
                .onErrorMap(e -> new Exception("Accounting sync failed: " + e.getMessage()));
    }
}
