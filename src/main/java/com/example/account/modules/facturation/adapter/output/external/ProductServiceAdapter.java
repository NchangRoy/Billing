package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.core.exception.SalesCoreErrorMapper;
import com.example.account.modules.facturation.domain.port.output.ProductServicePort;
import com.example.account.modules.facturation.dto.request.CreateProductRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductDefinitionResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import com.example.account.modules.shared.dto.kernel.KernelProductDefinitionResponse;
import com.example.account.modules.facturation.model.entity.ProductSaleSizeConfig;
import com.example.account.modules.facturation.repository.ProductSaleSizeConfigRepository;
import com.example.account.modules.shared.dto.kernel.KernelApiResponse;
import com.example.account.modules.shared.dto.kernel.KernelProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ProductServiceAdapter implements ProductServicePort {

    private final WebClient salesCoreWebClient;
    private final ProductSaleSizeConfigRepository saleSizeConfigRepository;
    private final R2dbcEntityTemplate entityTemplate;

    private static final ParameterizedTypeReference<KernelApiResponse<List<KernelProductResponse>>> PRODUCT_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<KernelProductResponse>> PRODUCT_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<Map<String, UUID>>> PROVISION_RESULT_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<List<KernelProductDefinitionResponse>>> PRODUCT_DEFINITION_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    public ProductServiceAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient,
                                  ProductSaleSizeConfigRepository saleSizeConfigRepository,
                                  R2dbcEntityTemplate entityTemplate) {
        this.salesCoreWebClient = salesCoreWebClient;
        this.saleSizeConfigRepository = saleSizeConfigRepository;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<ProductResponse> createProduct(UUID organizationId, CreateProductRequest request) {
        log.info("Provisioning sellable product '{}' for organization {}", request.getNomProduit(), organizationId);
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("organizationId", organizationId);
        body.put("globalCode", request.getReference());
        body.put("name", request.getNomProduit());
        body.put("familyCode", request.getTypeProduit());
        body.put("categoryCode", request.getCategorie());
        body.put("barcode", request.getCodeBarre());
        body.put("uomCode", request.getUom());
        body.put("imageUri", request.getPhoto());
        body.put("unitPrice", request.getPrixVente());
        body.put("currency", request.getCurrency());
        body.put("cost", request.getCout());
        body.put("quantity", request.getQuantity());
        body.put("minStockLevel", request.getMinStockLevel());
        body.put("maxStockLevel", request.getMaxStockLevel());

        return salesCoreWebClient
                .post()
                .uri("/api/product-core/sellable-products")
                .header("X-Organization-Id", organizationId.toString())
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("sales-core provision product error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(PROVISION_RESULT_TYPE)
                .map(KernelApiResponse::getData)
                .flatMap(ids -> {
                    UUID createdId = ids.containsKey("productId") ? ids.get("productId")
                            : ids.values().stream().findFirst().orElse(null);
                    if (createdId == null) {
                        return Mono.error(new RuntimeException("sales-core provision product: no id returned in " + ids));
                    }
                    // vatApplicable has no Kernel field — stash it in the local config row now,
                    // same mechanism updateSaleSizes() uses, so fetchProductById's merge picks
                    // it up immediately instead of silently defaulting to true.
                    return upsertConfig(createdId, organizationId, null, request.getVatApplicable(), null, request.getProductNumber())
                            .then(fetchProductById(createdId));
                });
    }

    @Override
    public Flux<ProductDefinitionResponse> fetchProductDefinitions(UUID organizationId) {
        log.info("Fetching product catalog definitions for organization: {}", organizationId);
        // /api/product-core/products (ProductView) — org-scoped implicitly via the
        // X-Organization-Id header (its only query param is "kind"). No price/cost/stock
        // here; this is purely for picking an existing definition to provision as sellable.
        return salesCoreWebClient
                .get()
                .uri("/api/product-core/products")
                .header("X-Organization-Id", organizationId.toString())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("sales-core product definitions error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(PRODUCT_DEFINITION_LIST_TYPE)
                .map(KernelApiResponse::getData)
                .flatMapMany(Flux::fromIterable)
                .map(ProductServiceAdapter::toProductDefinitionResponse);
    }

    static ProductDefinitionResponse toProductDefinitionResponse(KernelProductDefinitionResponse k) {
        return ProductDefinitionResponse.builder()
                .id(k.getId())
                .globalCode(k.getGlobalCode())
                .name(k.getName())
                .shortDescription(k.getShortDescription())
                .longDescription(k.getLongDescription())
                .nature(k.getNature())
                .articleKind(k.getArticleKind())
                .lifecycleStatus(k.getLifecycleStatus())
                .defaultUomId(k.getDefaultUomId())
                .categoryId(k.getCategoryId())
                .launchDate(k.getLaunchDate())
                .discontinueDate(k.getDiscontinueDate())
                .build();
    }

    @Override
    public Flux<ProductResponse> fetchProductsByOrganization(UUID organizationId) {
        log.info("Fetching products from sales-core for organization: {}", organizationId);
        // /api/product-core/products (ProductView) is the organization-agnostic catalog
        // definition — no price/cost/quantity/barcode/image field at all. The per-organization
        // listing with that data, which createProduct() below actually provisions into, is
        // /api/product-core/sellable-products (SellableProduct).
        return salesCoreWebClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/product-core/sellable-products")
                        .queryParam("organizationId", organizationId)
                        .build())
                .header("X-Organization-Id", organizationId.toString())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("sales-core products error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(PRODUCT_LIST_TYPE)
                .map(KernelApiResponse::getData)
                .flatMapMany(Flux::fromIterable)
                .map(this::mapToProductResponse)
                .collectList()
                .flatMapMany(this::mergeLocalConfigs);
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
        log.info("Fetching product {} from sales-core", productId);
        return salesCoreWebClient
                .get()
                .uri("/api/product-core/sellable-products/{id}", productId)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        resp -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId)))
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(err -> Mono.error(new RuntimeException("sales-core product error: " + SalesCoreErrorMapper.extractMessage(err)))))
                .bodyToMono(PRODUCT_TYPE)
                .map(KernelApiResponse::getData)
                .map(this::mapToProductResponse)
                .flatMap(product -> saleSizeConfigRepository.findByProductId(productId)
                        .map(config -> applyConfig(product, config))
                        .defaultIfEmpty(product));
    }

    /**
     * Batch-fetches local configs for every product id in one query rather
     * than per-product, since this backs the organization-wide list endpoint.
     */
    private Flux<ProductResponse> mergeLocalConfigs(List<ProductResponse> products) {
        List<UUID> productIds = products.stream().map(ProductResponse::getIdProduit).toList();
        return saleSizeConfigRepository.findByProductIdIn(productIds)
                .collectMap(ProductSaleSizeConfig::getProductId, c -> c)
                .flatMapMany(configsById -> Flux.fromIterable(products)
                        .map(product -> {
                            ProductSaleSizeConfig config = configsById.get(product.getIdProduit());
                            return config == null ? product : applyConfig(product, config);
                        }));
    }

    private ProductResponse applyConfig(ProductResponse product, ProductSaleSizeConfig config) {
        if (config.getPhoto() != null) {
            product.setPhoto(config.getPhoto());
        }
        product.setVatApplicable(config.isVatApplicable());
        product.setAllowedSaleSizes(config.getSaleSizes() != null ? config.getSaleSizes() : List.of());
        product.setProductNumber(config.getProductNumber());
        return product;
    }

    /**
     * Sale sizes, image, and VAT applicability are Billing's own local
     * config, not a Kernel product-core field — product-core's "variants"
     * are a generic sku/name concept with no price/image/VAT field anywhere
     * in the new product schemas. Upserts the config row, then returns the
     * full product (Kernel catalog data + this config merged in).
     */
    @Override
    public Mono<ProductResponse> updateSaleSizes(UUID productId, List<ProductResponse.SaleSize> allowedSaleSizes,
                                                  String photo, Boolean vatApplicable) {
        log.info("Updating sale-size config for product {}", productId);
        return ReactiveOrganizationContext.getOrganizationId()
                .flatMap(organizationId -> upsertConfig(productId, organizationId, photo, vatApplicable, allowedSaleSizes, null))
                .then(fetchProductById(productId));
    }

    @Override
    public Mono<String> updatePhoto(UUID productId, String photo) {
        log.info("Updating photo for product {}", productId);
        return ReactiveOrganizationContext.getOrganizationId()
                .flatMap(organizationId -> upsertConfig(productId, organizationId, photo, null, null, null))
                .thenReturn(photo);
    }

    private Mono<ProductSaleSizeConfig> upsertConfig(UUID productId, UUID organizationId, String photo,
                                                       Boolean vatApplicable, List<ProductResponse.SaleSize> saleSizes,
                                                       String productNumber) {
        LocalDateTime now = LocalDateTime.now();
        return saleSizeConfigRepository.findByProductId(productId)
                .flatMap(existing -> applyAndSave(existing, photo, vatApplicable, saleSizes, productNumber, now))
                .switchIfEmpty(Mono.defer(() -> {
                    ProductSaleSizeConfig created = ProductSaleSizeConfig.builder()
                            .id(UUID.randomUUID())
                            .productId(productId)
                            .organizationId(organizationId)
                            .photo(photo)
                            .vatApplicable(vatApplicable == null || vatApplicable)
                            .saleSizes(saleSizes != null ? saleSizes : List.of())
                            .productNumber(productNumber)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return entityTemplate.insert(created)
                            // product_id has a unique constraint — lost the race against a
                            // concurrent insert for the same product (e.g. a double-click),
                            // so fall back to updating the row the other request just created.
                            .onErrorResume(DataIntegrityViolationException.class, e ->
                                    saleSizeConfigRepository.findByProductId(productId)
                                            .flatMap(existing -> applyAndSave(existing, photo, vatApplicable, saleSizes, productNumber, now)));
                }));
    }

    private Mono<ProductSaleSizeConfig> applyAndSave(ProductSaleSizeConfig existing, String photo, Boolean vatApplicable,
                                                       List<ProductResponse.SaleSize> saleSizes, String productNumber, LocalDateTime now) {
        if (photo != null) existing.setPhoto(photo);
        if (vatApplicable != null) existing.setVatApplicable(vatApplicable);
        if (saleSizes != null) existing.setSaleSizes(saleSizes);
        if (productNumber != null) existing.setProductNumber(productNumber);
        existing.setUpdatedAt(now);
        // @Builder.Default's "true" initializer doesn't survive R2DBC hydrating this row
        // through the no-args constructor, so isNew() comes back true even for an existing
        // row — save() then tries an INSERT with this row's own id and collides with its
        // own primary key. Force it false here since we only ever reach this branch for a
        // row findByProductId() just proved already exists.
        existing.setNewEntity(false);
        return saleSizeConfigRepository.save(existing);
    }

    private ProductResponse mapToProductResponse(KernelProductResponse k) {
        return ProductResponse.builder()
                .idProduit(k.getProductId())
                .organizationId(k.getOrganizationId())
                .nomProduit(k.getName())
                .typeProduit(k.getFamilyCode())
                .prixVente(k.getUnitPrice())
                .cout(k.getCost() != null ? k.getCost() : k.getUnitPrice())
                .categorie(k.getCategoryCode() != null ? k.getCategoryCode() : k.getFamilyCode())
                .reference(k.getGlobalCode())
                .codeBarre(k.getBarcode())
                .photo(k.getImageUri())
                .active(k.getLifecycleStatus() == null || "ACTIVE".equalsIgnoreCase(k.getLifecycleStatus()))
                .uom(k.getUomCode())
                .stockQuantity(k.getQuantity())
                // Kernel's catalog only exposes a single total quantity; the available/reserved
                // split is tracked separately, locally, by the stock-reservation websocket flow.
                .availableQuantity(null)
                .reservedQuantity(null)
                .allowedSaleSizes(k.getAllowedSaleSizes() != null ? k.getAllowedSaleSizes() : List.of())
                .activePromotions(List.of())
                .vatApplicable(true)
                .createdAt(parseDate(k.getCreatedAt()))
                .updatedAt(parseDate(k.getUpdatedAt()))
                .build();
    }

    private java.time.LocalDate parseDate(String isoDateTime) {
        if (isoDateTime == null) return null;
        try {
            return java.time.OffsetDateTime.parse(isoDateTime).toLocalDate();
        } catch (Exception e) {
            try {
                return java.time.LocalDateTime.parse(isoDateTime).toLocalDate();
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
