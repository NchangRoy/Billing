package com.example.account.modules.core.domain.port.input;

import com.example.account.modules.core.domain.model.Organization;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrganizationUseCase {
    Flux<Organization> getAllOrganizations();
    Mono<Organization> getOrganizationById(UUID id);
    Mono<Organization> getOrganizationByCode(String code);
    Flux<Organization> getUserOrganizations(UUID userId);
    Mono<Organization> createOrganization(Organization organization, UUID creatorUserId);
    Mono<Organization> createOrganization(Organization organization);
    Mono<Organization> updateOrganization(UUID id, Organization organizationDetails);
    Mono<Void> deleteOrganization(UUID id);
}
