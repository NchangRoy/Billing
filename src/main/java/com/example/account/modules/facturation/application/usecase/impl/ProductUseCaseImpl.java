package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.port.input.ProductUseCase;
import com.example.account.modules.facturation.domain.port.output.ProductServicePort;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductUseCaseImpl implements ProductUseCase {

    private final ProductServicePort productServicePort;

    @Override
    public Flux<ProductResponse> getAllProducts() {
        return productServicePort.fetchAllProducts();
    }

    @Override
    public Flux<ProductResponse> getProductsByOrganization(UUID organizationId) {
        return productServicePort.fetchProductsByOrganization(organizationId);
    }

    @Override
    public Mono<ProductResponse> getProductById(UUID productId) {
        return productServicePort.fetchAllProducts()
                .filter(p -> productId.equals(p.getIdProduit()))
                .next()
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + productId)));
    }
}
