package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.domain.port.input.ProductCatalogUseCase;
import com.example.account.modules.facturation.dto.request.CreateCategoryRequest;
import com.example.account.modules.facturation.dto.request.CreateUomRequest;
import com.example.account.modules.facturation.dto.request.DefineProductRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.CategoryResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductDefinitionResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.UomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-catalog")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Catalog Settings", description = "Units of measure, categories, and product definitions — platform-wide catalog data, proxied to sales-core")
public class ProductCatalogController {

    private final ProductCatalogUseCase productCatalogUseCase;

    @GetMapping("/units")
    @Operation(summary = "List units of measure")
    public Flux<UomResponse> listUoms() {
        return productCatalogUseCase.listUoms();
    }

    @PostMapping("/units")
    @Operation(summary = "Create a unit of measure")
    public Mono<UomResponse> createUom(@Valid @RequestBody CreateUomRequest request) {
        return productCatalogUseCase.createUom(request);
    }

    @GetMapping("/categories")
    @Operation(summary = "List product categories")
    public Flux<CategoryResponse> listCategories() {
        return productCatalogUseCase.listCategories();
    }

    @PostMapping("/categories")
    @Operation(summary = "Create a product category")
    public Mono<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return productCatalogUseCase.createCategory(request);
    }

    @PostMapping("/products")
    @Operation(summary = "Define a new product in the catalog (not yet sellable — see POST /api/products to provision pricing/stock for it)")
    public Mono<ProductDefinitionResponse> defineProduct(@Valid @RequestBody DefineProductRequest request) {
        return productCatalogUseCase.defineProduct(request);
    }

    @PutMapping("/products/{productId}/category")
    @Operation(summary = "Assign a category to a catalog product")
    public Mono<Void> categorizeProduct(@PathVariable UUID productId, @RequestBody Map<String, UUID> body) {
        return productCatalogUseCase.categorizeProduct(productId, body.get("categoryId"));
    }
}
