package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.facturation.domain.port.input.ProductUseCase;
import com.example.account.modules.facturation.domain.port.output.ProductServicePort;
import com.example.account.modules.facturation.dto.request.CreateProductRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductDefinitionResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductUseCaseImpl implements ProductUseCase {

    private final ProductServicePort productServicePort;

    @Override
    public Mono<ProductResponse> createProduct(CreateProductRequest request) {
        if (request.getVatApplicable() == null) {
            request.setVatApplicable(true);
        }
        return ReactiveOrganizationContext.getOrganizationId()
                .flatMap(organizationId -> productServicePort.createProduct(organizationId, request));
    }

    @Override
    public Flux<ProductDefinitionResponse> getProductDefinitions() {
        return ReactiveOrganizationContext.getOrganizationId()
                .flatMapMany(productServicePort::fetchProductDefinitions);
    }

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
        return productServicePort.fetchProductById(productId);
    }

    @Override
    public Mono<ProductResponse> updateSaleSizes(UUID productId, List<ProductResponse.SaleSize> allowedSaleSizes, String photo, Boolean vatApplicable) {
        return productServicePort.updateSaleSizes(productId, allowedSaleSizes, photo, vatApplicable);
    }

    @Override
    public Mono<String> updatePhoto(UUID productId, String photo) {
        return productServicePort.updatePhoto(productId, photo);
    }
}
