package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ProductUseCase {
    Flux<ProductResponse> getAllProducts();
    Flux<ProductResponse> getProductsByOrganization(UUID organizationId);
    Mono<ProductResponse> getProductById(UUID productId);
    Mono<List<ProductResponse.SaleSize>> updateSaleSizes(UUID productId, List<ProductResponse.SaleSize> allowedSaleSizes);
    Mono<String> updatePhoto(UUID productId, String photo);
}
