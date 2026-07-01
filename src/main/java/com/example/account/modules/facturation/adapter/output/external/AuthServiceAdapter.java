package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.facturation.domain.port.output.AuthServicePort;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import com.example.account.modules.shared.dto.kernel.KernelActorResponse;
import com.example.account.modules.shared.dto.kernel.KernelApiResponse;
import com.example.account.modules.shared.dto.kernel.KernelAuthData;
import com.example.account.modules.shared.dto.kernel.KernelOrganizationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AuthServiceAdapter implements AuthServicePort {

    private final WebClient kernelWebClient;

    public AuthServiceAdapter(@Qualifier("kernelWebClient") WebClient kernelWebClient) {
        this.kernelWebClient = kernelWebClient;
    }

    private static final ParameterizedTypeReference<KernelApiResponse<KernelAuthData>> AUTH_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<KernelApiResponse<KernelActorResponse>> ACTOR_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<KernelApiResponse<List<KernelOrganizationResponse>>> ORG_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    @Override
    public Mono<SellerAuthResponse> login(String username, String password) {
        log.info("Authenticating seller '{}' against Kernel", username);

        Map<String, String> body = Map.of("principal", username, "password", password);

        return kernelWebClient
                .post()
                .uri("/api/auth/login")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        resp -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY, "Kernel auth error: " + err))))
                .bodyToMono(AUTH_TYPE)
                .flatMap(resp -> {
                    KernelAuthData auth = resp.getData();
                    if (auth == null || auth.getAccessToken() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed"));
                    }
                    if ("CONFIRM_MFA".equals(auth.getNextStep())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                "MFA required — use /api/auth/login/mfa/confirm with mfaToken: " + auth.getMfaToken()));
                    }
                    log.info("Kernel auth succeeded for '{}', fetching actor + org profile", username);
                    return fetchActorAndOrg(auth);
                });
    }

    private Mono<SellerAuthResponse> fetchActorAndOrg(KernelAuthData auth) {
        String bearer = "Bearer " + auth.getAccessToken();

        Mono<KernelActorResponse> actorMono = kernelWebClient
                .get()
                .uri("/api/actors/me")
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .retrieve()
                .bodyToMono(ACTOR_TYPE)
                .map(r -> r.getData())
                .onErrorResume(e -> {
                    log.warn("Could not fetch actor profile: {}", e.getMessage());
                    return Mono.empty();
                });

        Mono<KernelOrganizationResponse> orgMono = kernelWebClient
                .get()
                .uri("/api/organizations/my")
                .header(HttpHeaders.AUTHORIZATION, bearer)
                .retrieve()
                .bodyToMono(ORG_LIST_TYPE)
                .mapNotNull(r -> r.getData() != null && !r.getData().isEmpty() ? r.getData().get(0) : null)
                .onErrorResume(e -> {
                    log.warn("Could not fetch organization profile: {}", e.getMessage());
                    return Mono.empty();
                });

        return Mono.zip(
                actorMono.defaultIfEmpty(new KernelActorResponse()),
                orgMono.defaultIfEmpty(new KernelOrganizationResponse())
        ).map(tuple -> mapToSellerAuthResponse(auth, tuple.getT1(), tuple.getT2()));
    }

    private SellerAuthResponse mapToSellerAuthResponse(KernelAuthData auth,
                                                        KernelActorResponse actor,
                                                        KernelOrganizationResponse org) {
        SellerAuthResponse r = new SellerAuthResponse();
        r.setAccessToken(auth.getAccessToken());
        r.setId(auth.getActorId() != null ? auth.getActorId() : actor.getActorId());
        r.setUsername(actor.getName());

        r.setOrganizationId(org.getId());
        r.setOrganizationName(org.getDisplayName() != null ? org.getDisplayName() : org.getLegalName());
        r.setOrganizationLogoUri(org.getLogoUri());
        r.setOrganizationEmail(org.getEmail());
        r.setTaxNumber(org.getTaxNumber());

        r.setCreatedAt(actor.getCreatedAt());
        return r;
    }
}
