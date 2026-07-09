package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.domain.port.input.SellerAdminUseCase;
import com.example.account.modules.facturation.dto.request.AssignAgencyRequest;
import com.example.account.modules.facturation.dto.request.CreateSellerRequest;
import com.example.account.modules.facturation.dto.request.SellerUIPermissionsRequest;
import com.example.account.modules.facturation.dto.request.UpdateSellerPermissionsRequest;
import com.example.account.modules.facturation.dto.request.UpdateSellerPhotoRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.AssignAgencyResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.CreateSellerResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerListItemResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerUIPermissionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Seller Admin", description = "Seller creation, agency assignment, and per-seller UI permissions")
public class SellerAdminController {

    private final SellerAdminUseCase sellerAdminUseCase;

    @GetMapping
    @Operation(summary = "List sellers for an organization")
    public Flux<SellerListItemResponse> getAll(@RequestParam UUID organizationId) {
        return sellerAdminUseCase.listSellers(organizationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a seller")
    public Mono<CreateSellerResponse> create(@Valid @RequestBody CreateSellerRequest request) {
        return sellerAdminUseCase.createSeller(request);
    }

    @PostMapping("/{sellerId}/agency")
    @Operation(summary = "Assign a seller to an agency")
    public Mono<AssignAgencyResponse> assignAgency(@PathVariable UUID sellerId, @Valid @RequestBody AssignAgencyRequest request) {
        return sellerAdminUseCase.assignAgency(sellerId, request);
    }

    @GetMapping("/{sellerId}/ui-permissions")
    @Operation(summary = "Get a seller's UI permissions")
    public Mono<SellerUIPermissionsResponse> getUIPermissions(@PathVariable UUID sellerId) {
        return sellerAdminUseCase.getUIPermissions(sellerId);
    }

    @PostMapping("/{sellerId}/ui-permissions")
    @Operation(summary = "Set a seller's UI permissions")
    public Mono<SellerUIPermissionsResponse> setUIPermissions(@PathVariable UUID sellerId, @Valid @RequestBody SellerUIPermissionsRequest request) {
        return sellerAdminUseCase.setUIPermissions(sellerId, request);
    }

    @PutMapping("/{sellerId}/permissions")
    @Operation(summary = "Update a seller's sale permissions and permitted sale sizes")
    public Mono<SellerListItemResponse> updatePermissions(@PathVariable UUID sellerId, @Valid @RequestBody UpdateSellerPermissionsRequest request) {
        return sellerAdminUseCase.updatePermissions(sellerId, request);
    }

    @PutMapping("/{sellerId}/photo")
    @Operation(summary = "Update a seller's profile image")
    public Mono<SellerListItemResponse> updatePhoto(@PathVariable UUID sellerId, @Valid @RequestBody UpdateSellerPhotoRequest request) {
        return sellerAdminUseCase.updatePhoto(sellerId, request);
    }

    @DeleteMapping("/{sellerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a seller from the organization")
    public Mono<Void> deleteSeller(@PathVariable UUID sellerId) {
        return sellerAdminUseCase.deleteSeller(sellerId);
    }
}
