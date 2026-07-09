package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.AssignAgencyRequest;
import com.example.account.modules.facturation.dto.request.CreateSellerRequest;
import com.example.account.modules.facturation.dto.request.SellerUIPermissionsRequest;
import com.example.account.modules.facturation.dto.request.UpdateSellerPermissionsRequest;
import com.example.account.modules.facturation.dto.request.UpdateSellerPhotoRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.AssignAgencyResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.CreateSellerResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerListItemResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerUIPermissionsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SellerAdminUseCase {
    Flux<SellerListItemResponse> listSellers(UUID organizationId);
    Mono<CreateSellerResponse> createSeller(CreateSellerRequest request);
    Mono<AssignAgencyResponse> assignAgency(UUID sellerId, AssignAgencyRequest request);
    Mono<SellerUIPermissionsResponse> getUIPermissions(UUID sellerId);
    Mono<SellerUIPermissionsResponse> setUIPermissions(UUID sellerId, SellerUIPermissionsRequest request);
    Mono<SellerListItemResponse> updatePermissions(UUID sellerId, UpdateSellerPermissionsRequest request);
    Mono<SellerListItemResponse> updatePhoto(UUID sellerId, UpdateSellerPhotoRequest request);
    Mono<Void> deleteSeller(UUID sellerId);
}
