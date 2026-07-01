package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.facturation.domain.port.output.ProductServicePort;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import com.example.account.modules.shared.dto.kernel.KernelApiResponse;
import com.example.account.modules.shared.dto.kernel.KernelProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ProductServiceAdapter implements ProductServicePort {

    private final WebClient kernelWebClient;

    public ProductServiceAdapter(@Qualifier("kernelWebClient") WebClient kernelWebClient) {
        this.kernelWebClient = kernelWebClient;
    }

    private static final ParameterizedTypeReference<KernelApiResponse<List<KernelProductResponse>>> PRODUCT_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<KernelApiResponse<KernelProductResponse>> PRODUCT_TYPE =
            new ParameterizedTypeReference<>() {};

    @Override
    public Flux<ProductResponse> fetchProductsByOrganization(UUID organizationId) {
        log.info("Fetching products from Kernel for organization: {}", organizationId);
        return kernelWebClient
                .get()
                .uri(u -> u.path("/api/products").queryParam("organizationId", organizationId).build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("Kernel products error: " + err))))
                .bodyToMono(PRODUCT_LIST_TYPE)
                .flatMapMany(resp -> {
                    if (resp == null || resp.getData() == null) return Flux.empty();
                    return Flux.fromIterable(resp.getData());
                })
                .map(this::mapToProductResponse);
    }

    @Override
    public Flux<ProductResponse> fetchAllProducts() {
        return ReactiveOrganizationContext.getOrganizationIdOrEmpty()
                .flatMapMany(orgId -> fetchProductsByOrganization(orgId))
                .switchIfEmpty(Flux.defer(() -> {
                    log.warn("fetchAllProducts called without organization context — returning empty");
                    return Flux.empty();
                }));
    }

    @Override
    public Mono<ProductResponse> fetchProductById(UUID productId) {
        log.info("Fetching product {} from Kernel", productId);
        return kernelWebClient
                .get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        resp -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId)))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("Kernel product error: " + err))))
                .bodyToMono(PRODUCT_TYPE)
                .map(resp -> mapToProductResponse(resp.getData()));
    }

    private ProductResponse mapToProductResponse(KernelProductResponse k) {
        return ProductResponse.builder()
                .idProduit(k.getId())
                .organizationId(k.getOrganizationId())
                .nomProduit(k.getName())
                .typeProduit(k.getFamilyCode())
                .prixVente(k.getUnitPrice())
                .cout(k.getUnitPrice())
                .categorie(k.getCategory() != null ? k.getCategory() : k.getFamilyCode())
                .reference(k.getSku())
                .codeBarre(k.getBarcode())
                .photo(k.getPhotoUri())
                .active(k.getActive() != null ? k.getActive() : true)
                .uom(k.getUom())
                .stockQuantity(k.getStockQuantity())
                .availableQuantity(k.getAvailableQuantity())
                .reservedQuantity(k.getReservedQuantity())
                .allowedSaleSizes(List.of())
                .activePromotions(List.of())
                .build();
    }
}
