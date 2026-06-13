package com.example.account.modules.core.adapter.input.rest;

import com.example.account.modules.core.dto.OrganizationCreateRequest;
import com.example.account.modules.core.dto.OrganizationResponse;
import com.example.account.modules.core.domain.model.Organization;
import com.example.account.modules.core.domain.port.input.OrganizationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for organization management adapted for WebFlux.
 */
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organization Management", description = "Endpoints for managing organizations and memberships")
public class OrganizationRestController {

    private final OrganizationUseCase organizationUseCase;

    /**
     * Creates a new organization.
     */
    @PostMapping("/create")
    @Operation(summary = "Create new organization", description = "Creates a new organization and assigns creator as OWNER")
    public Mono<ResponseEntity<OrganizationResponse>> createOrganization(
            @Valid @RequestBody OrganizationCreateRequest request,
            @RequestParam UUID creatorUserId) {

        log.info("Creating organization: code={}, creator={}", request.getCode(), creatorUserId);

        Organization organization = mapToEntity(request);
        return organizationUseCase.createOrganization(organization, creatorUserId)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(created)));
    }

    /**
     * Gets an organization by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public Mono<ResponseEntity<OrganizationResponse>> getOrganization(@PathVariable UUID id) {
        return organizationUseCase.getOrganizationById(id)
                .map(organization -> ResponseEntity.ok(mapToResponse(organization)));
    }

    /**
     * Gets all organizations for a user.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's organizations", description = "Retrieves all organizations the user is a member of")
    public Flux<OrganizationResponse> getUserOrganizations(@PathVariable UUID userId) {
        return organizationUseCase.getUserOrganizations(userId)
                .map(this::mapToResponse);
    }

    // Mapping methods
    private Organization mapToEntity(OrganizationCreateRequest request) {
        Organization org = new Organization();
        org.setCode(request.getCode());
        org.setShortName(request.getShortName());
        org.setLongName(request.getLongName());
        org.setDescription(request.getDescription());
        org.setLogo(request.getLogo());
        org.setIsActive(true);
        org.setIsIndividual(request.getIsIndividual());
        org.setIsPublic(request.getIsPublic());
        org.setIsBusiness(request.getIsBusiness());
        org.setCountry(request.getCountry());
        org.setCity(request.getCity());
        org.setAddress(request.getAddress());
        org.setLocalization(request.getLocalization());
        org.setOpenTime(request.getOpenTime());
        org.setCloseTime(request.getCloseTime());
        org.setEmail(request.getEmail());
        org.setPhone(request.getPhone());
        org.setWhatsapp(request.getWhatsapp());
        org.setSocialNetworks(request.getSocialNetworks());
        org.setCapitalShare(request.getCapitalShare());
        org.setTaxNumber(request.getTaxNumber());
        return org;
    }

    private OrganizationResponse mapToResponse(Organization org) {
        return OrganizationResponse.builder()
            .id(org.getId())
            .code(org.getCode())
            .shortName(org.getShortName())
            .longName(org.getLongName())
            .description(org.getDescription())
            .logo(org.getLogo())
            .isActive(org.getIsActive())
            .isIndividual(org.getIsIndividual())
            .isPublic(org.getIsPublic())
            .isBusiness(org.getIsBusiness())
            .country(org.getCountry())
            .city(org.getCity())
            .address(org.getAddress())
            .localization(org.getLocalization())
            .openTime(org.getOpenTime())
            .closeTime(org.getCloseTime())
            .email(org.getEmail())
            .phone(org.getPhone())
            .whatsapp(org.getWhatsapp())
            .socialNetworks(org.getSocialNetworks())
            .capitalShare(org.getCapitalShare())
            .taxNumber(org.getTaxNumber())
            .createdAt(org.getCreatedAt())
            .updatedAt(org.getUpdatedAt())
            .build();
    }
}
