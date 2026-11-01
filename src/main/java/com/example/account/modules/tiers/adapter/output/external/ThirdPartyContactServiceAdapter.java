package com.example.account.modules.tiers.adapter.output.external;

import com.example.account.modules.core.exception.SalesCoreErrorMapper;
import com.example.account.modules.shared.dto.kernel.KernelActorContactResponse;
import com.example.account.modules.shared.dto.kernel.KernelApiResponse;
import com.example.account.modules.shared.dto.kernel.KernelThirdPartyResponse;
import com.example.account.modules.tiers.domain.port.output.ThirdPartyContactServicePort;
import com.example.account.modules.tiers.dto.AddThirdPartyContactRequest;
import com.example.account.modules.tiers.dto.ThirdPartyContactResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class ThirdPartyContactServiceAdapter implements ThirdPartyContactServicePort {

    private final WebClient salesCoreWebClient;
    private final WebClient kernelWebClient;

    private static final ParameterizedTypeReference<KernelApiResponse<KernelThirdPartyResponse>> THIRD_PARTY_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<List<KernelActorContactResponse>>> CONTACT_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final Function<ClientResponse, Mono<? extends Throwable>> SALES_CORE_ERROR =
            SalesCoreErrorMapper.forContext("sales-core third-party error");

    public ThirdPartyContactServiceAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient,
                                            @Qualifier("kernelWebClient") WebClient kernelWebClient) {
        this.salesCoreWebClient = salesCoreWebClient;
        this.kernelWebClient = kernelWebClient;
    }

    @Override
    public Flux<ThirdPartyContactResponse> getContacts(UUID thirdPartyId) {
        return resolveActorId(thirdPartyId)
                .flatMapMany(this::fetchContacts)
                .onErrorResume(e -> {
                    log.warn("Could not resolve actor for third party {}: {}", thirdPartyId, e.getMessage());
                    return Flux.empty();
                });
    }

    @Override
    public Mono<ThirdPartyContactResponse> addContact(UUID thirdPartyId, AddThirdPartyContactRequest request) {
        return resolveActorId(thirdPartyId)
                .switchIfEmpty(Mono.error(new IllegalStateException(
                        "Third party " + thirdPartyId + " has no underlying actor to attach a contact to")))
                .flatMap(actorId -> createContact(actorId, request));
    }

    // Tried /api/customers/{id} first (same path KernelClientAdapter already
    // relies on) rather than the generic /api/third-parties/{id} directly:
    // Kernel's generic ThirdPartyController carries a class-level
    // @PreAuthorize requiring 'third-parties:write' on every method,
    // including its read-only GET, while the role-specific customer/
    // fournisseur controllers correctly accept read-or-write — so the
    // generic endpoint 401s for callers whose token would otherwise be
    // granted access. Falling back to the generic endpoint only when the
    // third party isn't a customer (e.g. a fournisseur-only record), same
    // as KernelFournisseurAdapter does.
    @Override
    public Mono<UUID> resolveActorId(UUID thirdPartyId) {
        return fetchPartyId("/api/customers/{thirdPartyId}", thirdPartyId)
                .onErrorResume(e -> fetchPartyId("/api/third-parties/{thirdPartyId}", thirdPartyId));
    }

    private Mono<UUID> fetchPartyId(String uriTemplate, UUID thirdPartyId) {
        return salesCoreWebClient
                .get()
                .uri(uriTemplate, thirdPartyId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(THIRD_PARTY_TYPE)
                .map(KernelApiResponse::getData)
                .mapNotNull(KernelThirdPartyResponse::getPartyId);
    }

    // Kernel's CreateContactRequest is a Java record with primitive boolean
    // fields (isEmailVerified/isPhoneNumberVerified/isFavorite) — omitting
    // any of them makes Jackson fail to bind null into a primitive, which
    // Kernel reports back as a generic, misleading 400 "Failed to read HTTP
    // message" instead of a field-specific error. Always send all three.
    private Mono<ThirdPartyContactResponse> createContact(UUID actorId, AddThirdPartyContactRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("contactableType", "ACTOR");
        payload.put("contactableId", actorId.toString());
        payload.put("firstName", request.getFirstName());
        payload.put("lastName", request.getLastName());
        payload.put("email", request.getEmail());
        payload.put("phoneNumber", request.getPhoneNumber());
        payload.put("isEmailVerified", false);
        payload.put("isPhoneNumberVerified", false);
        payload.put("isFavorite", request.isFavorite());

        return kernelWebClient
                .post()
                .uri("/api/contacts")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<KernelApiResponse<KernelActorContactResponse>>() {})
                .map(KernelApiResponse::getData)
                .map(c -> ThirdPartyContactResponse.builder()
                        .id(c.getId())
                        .firstName(c.getFirstName())
                        .lastName(c.getLastName())
                        .email(c.getEmail())
                        .phoneNumber(c.getPhoneNumber())
                        .favorite(c.isFavorite())
                        .build());
    }

    private Flux<ThirdPartyContactResponse> fetchContacts(UUID actorId) {
        return kernelWebClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/contacts")
                        .queryParam("contactableType", "ACTOR")
                        .queryParam("contactableId", actorId)
                        .build())
                .retrieve()
                .bodyToMono(CONTACT_LIST_TYPE)
                .map(KernelApiResponse::getData)
                .flatMapMany(Flux::fromIterable)
                .filter(c -> c.getEmail() != null && !c.getEmail().isBlank())
                .map(c -> ThirdPartyContactResponse.builder()
                        .id(c.getId())
                        .firstName(c.getFirstName())
                        .lastName(c.getLastName())
                        .email(c.getEmail())
                        .phoneNumber(c.getPhoneNumber())
                        .favorite(c.isFavorite())
                        .build())
                .onErrorResume(e -> {
                    log.warn("Failed to fetch contacts for actor {}: {}", actorId, e.getMessage());
                    return Flux.empty();
                });
    }
}
