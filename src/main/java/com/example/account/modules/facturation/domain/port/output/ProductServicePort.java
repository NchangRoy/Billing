package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ProductServicePort {
    Flux<ProductResponse> fetchProductsByOrganization(UUID organizationId);
    Flux<ProductResponse> fetchAllProducts();
}
