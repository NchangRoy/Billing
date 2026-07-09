package com.example.account.modules.tiers.adapter.output.external;

import com.example.account.modules.core.exception.SalesCoreErrorMapper;
import com.example.account.modules.tiers.domain.port.output.ThirdPartySaleConfigServicePort;
import com.example.account.modules.tiers.dto.SaleConfigResponse;
import com.example.account.modules.tiers.dto.SetSaleConfigRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class ThirdPartySaleConfigServiceAdapter implements ThirdPartySaleConfigServicePort {

    private final WebClient salesCoreWebClient;

    public ThirdPartySaleConfigServiceAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient) {
        this.salesCoreWebClient = salesCoreWebClient;
    }

    private static final Function<org.springframework.web.reactive.function.client.ClientResponse, Mono<? extends Throwable>> SALES_CORE_ERROR =
            SalesCoreErrorMapper.forContext("sales-core sale-config error");

    @Override
    public Mono<SaleConfigResponse> setConfig(UUID thirdPartyId, SetSaleConfigRequest request) {
        return salesCoreWebClient
                .post()
                .uri("/api/third-parties/{thirdPartyId}/sale-config", thirdPartyId)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SaleConfigResponse.class);
    }

    @Override
    public Mono<SaleConfigResponse> getConfig(UUID thirdPartyId) {
        return salesCoreWebClient
                .get()
                .uri("/api/third-parties/{thirdPartyId}/sale-config", thirdPartyId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(SaleConfigResponse.class);
    }
}
