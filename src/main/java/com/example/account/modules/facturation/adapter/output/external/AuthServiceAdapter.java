package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.core.exception.SalesCoreErrorMapper;
import com.example.account.modules.facturation.domain.port.output.AuthServicePort;
import com.example.account.modules.facturation.dto.request.ChangePasswordRequest;
import com.example.account.modules.facturation.dto.request.PinLoginRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import com.example.account.modules.shared.dto.kernel.KernelApiResponse;
import com.example.account.modules.shared.dto.kernel.KernelLoginResponse;
import com.example.account.modules.shared.dto.kernel.KernelOrganizationResponse;
import com.example.account.modules.shared.dto.kernel.SalesCoreSellerAuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Sellers now authenticate against sales-core's own local auth (email +
 * password, sales-core-issued JWT) instead of logging in to Kernel directly.
 * This is a genuinely different auth system from Kernel's — see sales-core's
 * SellerAuthService. "Try Out" is the one exception: it authenticates directly
 * against Kernel (see tryOut()) for people who already have a Kernel-registered
 * organization, then auto-provisions/reuses a local seller so the rest of the
 * app behaves exactly like a normal sales-core login.
 */
@Service
@Slf4j
public class AuthServiceAdapter implements AuthServicePort {

    private final WebClient salesCoreWebClient;
    private final WebClient kernelWebClient;

    private static final ParameterizedTypeReference<KernelApiResponse<KernelLoginResponse>> KERNEL_LOGIN_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<List<KernelOrganizationResponse>>> KERNEL_ORG_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    public AuthServiceAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient,
                               @Qualifier("kernelWebClient") WebClient kernelWebClient) {
        this.salesCoreWebClient = salesCoreWebClient;
        this.kernelWebClient = kernelWebClient;
    }

    @Override
    public Mono<SellerAuthResponse> login(String username, String password) {
        log.info("Authenticating seller '{}' against sales-core", username);

        Map<String, String> body = Map.of("email", username, "password", password);

        return salesCoreWebClient
                .post()
                .uri("/api/sellers/auth/login")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        resp -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY, "sales-core auth error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(SalesCoreSellerAuthResponse.class)
                .map(this::mapToSellerAuthResponse);
    }

    @Override
    public Mono<SellerAuthResponse> loginByPin(PinLoginRequest request) {
        log.info("Authenticating seller via PIN against sales-core, org {}", request.getOrganizationId());

        Map<String, Object> body = Map.of("organizationId", request.getOrganizationId(), "pin", request.getPin());

        return salesCoreWebClient
                .post()
                .uri("/api/sellers/auth/login-pin")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        resp -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid PIN")))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY, "sales-core auth error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(SalesCoreSellerAuthResponse.class)
                .map(this::mapToSellerAuthResponse);
    }

    @Override
    public Mono<SellerAuthResponse> changePassword(ChangePasswordRequest request) {
        log.info("Changing password for seller '{}' against sales-core", request.getEmail());

        return salesCoreWebClient
                .post()
                .uri("/api/sellers/auth/change-password")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        resp -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY, "sales-core auth error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(SalesCoreSellerAuthResponse.class)
                .map(this::mapToSellerAuthResponse);
    }

    @Override
    public Mono<SellerAuthResponse> tryOut(String principal, String password, java.util.UUID organizationId) {
        log.info("Try Out login for principal: {}", principal);

        return kernelWebClient
                .post()
                .uri("/api/auth/login")
                .bodyValue(Map.of("principal", principal, "password", password))
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        resp -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY, "Kernel auth error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(KERNEL_LOGIN_TYPE)
                .map(KernelApiResponse::getData)
                .flatMap(kernelUser -> kernelWebClient
                        .get()
                        .uri("/api/organizations/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + kernelUser.getAccessToken())
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                resp -> resp.bodyToMono(String.class)
                                        .flatMap(err -> Mono.error(new ResponseStatusException(
                                                HttpStatus.BAD_GATEWAY, "Kernel organizations lookup error: " + SalesCoreErrorMapper.extractMessage(err)))))
                        .bodyToMono(KERNEL_ORG_LIST_TYPE)
                        .map(KernelApiResponse::getData)
                        .flatMap(orgs -> completeTryOut(kernelUser, orgs, organizationId, password)));
    }

    /**
     * Decides how to finish the Try Out login given the account's Kernel orgs:
     * - organizationId given: must be one of the account's orgs (disambiguation from a prior "requires selection" response).
     * - none given, exactly one org: proceed with it directly, same as before this feature existed.
     * - none given, several orgs: don't log in to sales-core yet — hand back the list so the frontend can show a picker.
     */
    private Mono<SellerAuthResponse> completeTryOut(KernelLoginResponse kernelUser, List<KernelOrganizationResponse> orgs,
                                                     java.util.UUID organizationId, String password) {
        if (orgs == null || orgs.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No organization found for this account yet."));
        }
        if (organizationId != null) {
            return orgs.stream()
                    .filter(org -> organizationId.equals(org.getId()))
                    .findFirst()
                    .map(org -> tryOutLoginToSalesCore(kernelUser, org, password))
                    .orElseGet(() -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "This organization is not associated with the account.")));
        }
        if (orgs.size() == 1) {
            return tryOutLoginToSalesCore(kernelUser, orgs.get(0), password);
        }
        SellerAuthResponse selection = new SellerAuthResponse();
        selection.setRequiresOrganizationSelection(true);
        selection.setAvailableOrganizations(orgs);
        return Mono.just(selection);
    }

    private Mono<SellerAuthResponse> tryOutLoginToSalesCore(KernelLoginResponse kernelUser, KernelOrganizationResponse org, String password) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("username", kernelUser.getUsername());
        body.put("email", kernelUser.getEmail());
        body.put("password", password);
        body.put("organizationId", org.getId());
        body.put("organizationName", org.getDisplayName() != null ? org.getDisplayName() : org.getShortName());
        body.put("organizationLogoUri", org.getLogoUri());
        body.put("organizationEmail", org.getEmail());
        body.put("taxNumber", org.getTaxNumber());

        return salesCoreWebClient
                .post()
                .uri("/api/sellers/auth/try-out")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new ResponseStatusException(
                                        HttpStatus.BAD_GATEWAY, "sales-core try-out error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(SalesCoreSellerAuthResponse.class)
                .map(this::mapToSellerAuthResponse);
    }

    private SellerAuthResponse mapToSellerAuthResponse(SalesCoreSellerAuthResponse auth) {
        SellerAuthResponse r = new SellerAuthResponse();
        r.setAccessToken(auth.getAccessToken());
        r.setId(auth.getId());
        r.setUsername(auth.getUsername());
        r.setRole(auth.getRole());

        r.setAgency(auth.getAgency());
        r.setSalePoint(auth.getSalePoint());
        r.setPermissions(auth.getPermissions());
        r.setPermittedSaleSizes(auth.getPermittedSaleSizes());

        r.setOrganizationId(auth.getOrganizationId());
        r.setOrganizationName(auth.getOrganizationName());
        r.setOrganizationLogoUri(auth.getOrganizationLogoUri());
        r.setOrganizationEmail(auth.getOrganizationEmail());
        r.setTaxNumber(auth.getTaxNumber());

        r.setAgencyId(auth.getAgencyId());
        r.setAgencyEmail(auth.getAgencyEmail());
        r.setAgencyPhone(auth.getAgencyPhone());
        r.setAgencyCity(auth.getAgencyCity());
        r.setAgencyAddress(auth.getAgencyAddress());

        r.setSalesPointId(auth.getSalesPointId());
        r.setSalesPointAddress(auth.getSalesPointAddress());

        r.setMustChangePassword(auth.getMustChangePassword());
        r.setUiPermissions(auth.getUiPermissions());

        if (auth.getCreatedAt() != null) {
            r.setCreatedAt(Instant.parse(auth.getCreatedAt()));
        }
        return r;
    }
}
