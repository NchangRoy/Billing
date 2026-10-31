package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.request.CreateCategoryRequest;
import com.example.account.modules.facturation.dto.request.CreateUomRequest;
import com.example.account.modules.facturation.dto.request.DefineProductRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.CategoryResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductDefinitionResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.UomResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductCatalogServicePort {
    Flux<UomResponse> listUoms();
    Mono<UomResponse> createUom(CreateUomRequest request);
    Flux<CategoryResponse> listCategories(UUID organizationId);
    Mono<CategoryResponse> createCategory(UUID organizationId, CreateCategoryRequest request);
    Mono<ProductDefinitionResponse> defineProduct(UUID organizationId, DefineProductRequest request);
    Mono<Void> categorizeProduct(UUID productId, UUID categoryId);
}
