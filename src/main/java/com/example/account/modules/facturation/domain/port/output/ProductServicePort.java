package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.request.CreateProductRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductDefinitionResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ProductServicePort {
    Mono<ProductResponse> createProduct(UUID organizationId, CreateProductRequest request);
    Flux<ProductDefinitionResponse> fetchProductDefinitions(UUID organizationId);
    Flux<ProductResponse> fetchProductsByOrganization(UUID organizationId);
    Flux<ProductResponse> fetchAllProducts();
    Mono<ProductResponse> fetchProductById(UUID productId);
    Mono<ProductResponse> updateSaleSizes(UUID productId, List<ProductResponse.SaleSize> allowedSaleSizes, String photo, Boolean vatApplicable);
    Mono<String> updatePhoto(UUID productId, String photo);
}
