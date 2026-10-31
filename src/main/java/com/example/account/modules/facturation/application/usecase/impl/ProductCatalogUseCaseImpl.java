package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.facturation.domain.port.input.ProductCatalogUseCase;
import com.example.account.modules.facturation.domain.port.output.ProductCatalogServicePort;
import com.example.account.modules.facturation.dto.request.CreateCategoryRequest;
import com.example.account.modules.facturation.dto.request.CreateUomRequest;
import com.example.account.modules.facturation.dto.request.DefineProductRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.CategoryResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductDefinitionResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.UomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCatalogUseCaseImpl implements ProductCatalogUseCase {

    private final ProductCatalogServicePort productCatalogServicePort;

    @Override
    public Flux<UomResponse> listUoms() {
        return productCatalogServicePort.listUoms();
    }

    @Override
    public Mono<UomResponse> createUom(CreateUomRequest request) {
        return productCatalogServicePort.createUom(request);
    }

    @Override
    public Flux<CategoryResponse> listCategories() {
        return ReactiveOrganizationContext.getOrganizationId()
                .flatMapMany(productCatalogServicePort::listCategories);
    }

    @Override
    public Mono<CategoryResponse> createCategory(CreateCategoryRequest request) {
        return ReactiveOrganizationContext.getOrganizationId()
                .flatMap(organizationId -> productCatalogServicePort.createCategory(organizationId, request));
    }

    @Override
    public Mono<ProductDefinitionResponse> defineProduct(DefineProductRequest request) {
        return ReactiveOrganizationContext.getOrganizationId()
                .flatMap(organizationId -> productCatalogServicePort.defineProduct(organizationId, request));
    }

    @Override
    public Mono<Void> categorizeProduct(UUID productId, UUID categoryId) {
        return productCatalogServicePort.categorizeProduct(productId, categoryId);
    }
}
