package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.facturation.domain.port.output.SellerServicePort;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import com.example.account.modules.shared.dto.kernel.ApiResponseListThirdPartyResponse;
import com.example.account.modules.shared.dto.kernel.ThirdPartyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerServiceAdapter implements SellerServicePort {

    private final WebClient.Builder webClientBuilder;

    @Value("${comops.kernel.ip}")
    private String kernelIp;

    @Override
    public Flux<SellerAuthResponse> getSellersByOrganization(UUID organizationId) {
        String url = String.format(
                "http://%s/api/sales-agents?organizationId=%s",
                kernelIp,
                organizationId
        );
        log.info("Requesting sales agents from: {}", url);

        return webClientBuilder.build()
                .get()
                .uri(url)
                .header("X-Organization-ID", organizationId.toString())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.error("Error calling Kernel sales-agents: {}", response.statusCode());
                    return Mono.empty();
                })
                .bodyToMono(ApiResponseListThirdPartyResponse.class)
                .flatMapMany(resp -> {
                    if (resp == null || resp.getData() == null) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(resp.getData());
                })
                .map(this::mapThirdPartyToSellerAuthResponse)
                .doOnComplete(() -> log.info("Successfully requested sales agents."))
                .onErrorResume(e -> {
                    log.error("Error calling Kernel sales-agents: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    private SellerAuthResponse mapThirdPartyToSellerAuthResponse(ThirdPartyResponse tp) {
        SellerAuthResponse r = new SellerAuthResponse();
        r.setId(tp.getId());
        r.setUsername(tp.getDisplayName());
        r.setOrganizationId(tp.getOrganizationId() != null ? tp.getOrganizationId() : tp.getTenantId());
        return r;
    }
}
