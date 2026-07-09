package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.domain.port.input.ProductUseCase;
import com.example.account.modules.facturation.dto.request.UpdatePhotoRequest;
import com.example.account.modules.facturation.dto.request.UpdateSaleSizesRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product catalog endpoints")
public class ProductController {

    private final ProductUseCase productUseCase;

    @GetMapping
    @Operation(summary = "Get all products")
    public Flux<ProductResponse> getAllProducts() {
        log.info("Request to fetch all products");
        return productUseCase.getAllProducts();
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID")
    public Mono<ResponseEntity<ProductResponse>> getProductById(@PathVariable UUID productId) {
        log.info("Request to fetch product: {}", productId);
        return productUseCase.getProductById(productId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get products by organization")
    public Flux<ProductResponse> getProductsByOrganization(@PathVariable UUID organizationId) {
        log.info("Request to fetch products for organization: {}", organizationId);
        return productUseCase.getProductsByOrganization(organizationId);
    }

    @PutMapping("/{productId}/sale-sizes")
    @Operation(summary = "Configure a product's allowed sale sizes and per-size pricing")
    public Mono<List<ProductResponse.SaleSize>> updateSaleSizes(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateSaleSizesRequest request) {
        log.info("Request to update sale sizes for product: {}", productId);
        return productUseCase.updateSaleSizes(productId, request.getAllowedSaleSizes());
    }

    @PutMapping("/{productId}/photo")
    @Operation(summary = "Set a product's photo URL")
    public Mono<String> updatePhoto(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdatePhotoRequest request) {
        log.info("Request to update photo for product: {}", productId);
        return productUseCase.updatePhoto(productId, request.getPhoto());
    }
}
