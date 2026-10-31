package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.CreateProductRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductDefinitionResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ProductUseCase {
    Mono<ProductResponse> createProduct(CreateProductRequest request);
    Flux<ProductDefinitionResponse> getProductDefinitions();
    Flux<ProductResponse> getAllProducts();
    Flux<ProductResponse> getProductsByOrganization(UUID organizationId);
    Mono<ProductResponse> getProductById(UUID productId);
    Mono<ProductResponse> updateSaleSizes(UUID productId, List<ProductResponse.SaleSize> allowedSaleSizes, String photo, Boolean vatApplicable);
    Mono<String> updatePhoto(UUID productId, String photo);
}
