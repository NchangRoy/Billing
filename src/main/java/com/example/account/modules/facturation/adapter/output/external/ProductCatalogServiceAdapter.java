package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.core.exception.SalesCoreErrorMapper;
import com.example.account.modules.facturation.domain.port.output.ProductCatalogServicePort;
import com.example.account.modules.facturation.dto.request.CreateCategoryRequest;
import com.example.account.modules.facturation.dto.request.CreateUomRequest;
import com.example.account.modules.facturation.dto.request.DefineProductRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.CategoryResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductDefinitionResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.UomResponse;
import com.example.account.modules.shared.dto.kernel.KernelApiResponse;
import com.example.account.modules.shared.dto.kernel.KernelCategoryResponse;
import com.example.account.modules.shared.dto.kernel.KernelProductDefinitionResponse;
import com.example.account.modules.shared.dto.kernel.KernelUomResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ProductCatalogServiceAdapter implements ProductCatalogServicePort {

    private final WebClient salesCoreWebClient;

    private static final ParameterizedTypeReference<KernelApiResponse<List<KernelUomResponse>>> UOM_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<KernelUomResponse>> UOM_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<List<KernelCategoryResponse>>> CATEGORY_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<KernelCategoryResponse>> CATEGORY_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<KernelProductDefinitionResponse>> PRODUCT_DEFINITION_TYPE =
            new ParameterizedTypeReference<>() {};

    public ProductCatalogServiceAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient) {
        this.salesCoreWebClient = salesCoreWebClient;
    }

    @Override
    public Flux<UomResponse> listUoms() {
        return salesCoreWebClient
                .get()
                .uri("/api/product-core/units")
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("sales-core units error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(UOM_LIST_TYPE)
                .map(KernelApiResponse::getData)
                .flatMapMany(Flux::fromIterable)
                .map(this::toUomResponse);
    }

    @Override
    public Mono<UomResponse> createUom(CreateUomRequest request) {
        Map<String, Object> body = Map.of(
                "code", request.getCode(),
                "name", request.getName(),
                "dimension", request.getDimension(),
                "symbol", request.getSymbol() == null ? "" : request.getSymbol(),
                "baseFactor", request.getBaseFactor() == null ? 1 : request.getBaseFactor());

        return salesCoreWebClient
                .post()
                .uri("/api/product-core/units")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("sales-core create unit error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(UOM_TYPE)
                .map(KernelApiResponse::getData)
                .map(this::toUomResponse);
    }

    @Override
    public Flux<CategoryResponse> listCategories(UUID organizationId) {
        return salesCoreWebClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/product-core/classification/categories")
                        .queryParam("organizationId", organizationId)
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("sales-core categories error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(CATEGORY_LIST_TYPE)
                .map(KernelApiResponse::getData)
                .flatMapMany(Flux::fromIterable)
                .map(this::toCategoryResponse);
    }

    @Override
    public Mono<CategoryResponse> createCategory(UUID organizationId, CreateCategoryRequest request) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("code", request.getCode());
        body.put("name", request.getName());
        body.put("organizationId", organizationId);
        if (request.getParentId() != null) {
            body.put("parentId", request.getParentId());
        }

        return salesCoreWebClient
                .post()
                .uri("/api/product-core/classification/categories")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("sales-core create category error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(CATEGORY_TYPE)
                .map(KernelApiResponse::getData)
                .map(this::toCategoryResponse);
    }

    @Override
    public Mono<ProductDefinitionResponse> defineProduct(UUID organizationId, DefineProductRequest request) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("globalCode", request.getGlobalCode());
        body.put("name", request.getName());
        body.put("nature", request.getNature());
        body.put("defaultUomId", request.getDefaultUomId());
        if (request.getArticleKind() != null) {
            body.put("articleKind", request.getArticleKind());
        }
        if (request.getShortDescription() != null) {
            body.put("shortDescription", request.getShortDescription());
        }

        return salesCoreWebClient
                .post()
                .uri("/api/product-core/products")
                .header("X-Organization-Id", organizationId.toString())
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("sales-core define product error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(PRODUCT_DEFINITION_TYPE)
                .map(KernelApiResponse::getData)
                .map(ProductServiceAdapter::toProductDefinitionResponse);
    }

    @Override
    public Mono<Void> categorizeProduct(UUID productId, UUID categoryId) {
        return salesCoreWebClient
                .put()
                .uri("/api/product-core/products/{productId}/category", productId)
                .bodyValue(Map.of("categoryId", categoryId))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("sales-core categorize product error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .toBodilessEntity()
                .then();
    }

    private UomResponse toUomResponse(KernelUomResponse k) {
        return UomResponse.builder()
                .id(k.getId())
                .code(k.getCode())
                .name(k.getName())
                .symbol(k.getSymbol())
                .dimension(k.getDimension())
                .baseFactor(k.getBaseFactor())
                .base(k.getBase())
                .build();
    }

    private CategoryResponse toCategoryResponse(KernelCategoryResponse k) {
        return CategoryResponse.builder()
                .id(k.getId())
                .parentCategoryId(k.getParentCategoryId())
                .code(k.getCode())
                .name(k.getName())
                .path(k.getPath())
                .status(k.getStatus())
                .build();
    }
}
