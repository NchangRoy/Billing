package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.CreateCategoryRequest;
import com.example.account.modules.facturation.dto.request.CreateUomRequest;
import com.example.account.modules.facturation.dto.request.DefineProductRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.CategoryResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductDefinitionResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.UomResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductCatalogUseCase {
    Flux<UomResponse> listUoms();
    Mono<UomResponse> createUom(CreateUomRequest request);
    Flux<CategoryResponse> listCategories();
    Mono<CategoryResponse> createCategory(CreateCategoryRequest request);
    Mono<ProductDefinitionResponse> defineProduct(DefineProductRequest request);
    Mono<Void> categorizeProduct(UUID productId, UUID categoryId);
}
