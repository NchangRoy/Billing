package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ProductServicePort {
    Flux<ProductResponse> fetchProductsByOrganization(UUID organizationId);
    Flux<ProductResponse> fetchAllProducts();
    Mono<ProductResponse> fetchProductById(UUID productId);
    Mono<List<ProductResponse.SaleSize>> updateSaleSizes(UUID productId, List<ProductResponse.SaleSize> allowedSaleSizes);
    Mono<String> updatePhoto(UUID productId, String photo);
}
