package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.port.input.SellerAdminUseCase;
import com.example.account.modules.facturation.domain.port.output.SellerServicePort;
import com.example.account.modules.facturation.dto.request.AssignAgencyRequest;
import com.example.account.modules.facturation.dto.request.CreateSellerRequest;
import com.example.account.modules.facturation.dto.request.SellerUIPermissionsRequest;
import com.example.account.modules.facturation.dto.request.UpdateSellerPermissionsRequest;
import com.example.account.modules.facturation.dto.request.UpdateSellerPhotoRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.AssignAgencyResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.CreateSellerResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerListItemResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerUIPermissionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerAdminUseCaseImpl implements SellerAdminUseCase {

    private final SellerServicePort sellerServicePort;

    @Override
    public Flux<SellerListItemResponse> listSellers(UUID organizationId) {
        return sellerServicePort.listSellers(organizationId);
    }

    @Override
    public Mono<CreateSellerResponse> createSeller(CreateSellerRequest request) {
        return sellerServicePort.createSeller(request);
    }

    @Override
    public Mono<AssignAgencyResponse> assignAgency(UUID sellerId, AssignAgencyRequest request) {
        return sellerServicePort.assignAgency(sellerId, request);
    }

    @Override
    public Mono<SellerUIPermissionsResponse> getUIPermissions(UUID sellerId) {
        return sellerServicePort.getUIPermissions(sellerId);
    }

    @Override
    public Mono<SellerUIPermissionsResponse> setUIPermissions(UUID sellerId, SellerUIPermissionsRequest request) {
        return sellerServicePort.setUIPermissions(sellerId, request);
    }

    @Override
    public Mono<SellerListItemResponse> updatePermissions(UUID sellerId, UpdateSellerPermissionsRequest request) {
        return sellerServicePort.updatePermissions(sellerId, request);
    }

    @Override
    public Mono<SellerListItemResponse> updatePhoto(UUID sellerId, UpdateSellerPhotoRequest request) {
        return sellerServicePort.updatePhoto(sellerId, request);
    }

    @Override
    public Mono<Void> deleteSeller(UUID sellerId) {
        return sellerServicePort.deleteSeller(sellerId);
    }
}
