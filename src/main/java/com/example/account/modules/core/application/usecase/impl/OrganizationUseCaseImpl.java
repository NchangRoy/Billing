package com.example.account.modules.core.application.usecase.impl;

import com.example.account.modules.core.domain.model.Organization;
import com.example.account.modules.core.domain.model.UserOrganization;
import com.example.account.modules.core.model.enums.OrganizationRole;
import com.example.account.modules.core.domain.port.output.OrganizationRepositoryPort;
import com.example.account.modules.core.domain.port.output.UserOrganizationRepositoryPort;
import com.example.account.modules.core.domain.port.output.UserRepositoryPort;
import com.example.account.modules.core.domain.port.input.OrganizationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationUseCaseImpl implements OrganizationUseCase {

    private final OrganizationRepositoryPort organizationRepositoryPort;
    private final UserOrganizationRepositoryPort userOrganizationRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public Flux<Organization> getAllOrganizations() {
        log.info("Récupération de toutes les organisations");
        return organizationRepositoryPort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Organization> getOrganizationById(UUID id) {
        log.info("Récupération de l'organisation avec l'ID : {}", id);
        return organizationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Organisation non trouvée avec l'ID : " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Organization> getOrganizationByCode(String code) {
        log.info("Récupération de l'organisation avec le code : {}", code);
        return organizationRepositoryPort.findByCode(code)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Organisation non trouvée avec le code : " + code)));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Organization> getUserOrganizations(UUID userId) {
        log.info("Récupération des organisations pour l'utilisateur : {}", userId);
        return userOrganizationRepositoryPort.findActiveByUserId(userId)
                .flatMap(uo -> organizationRepositoryPort.findById(uo.getOrganizationId()));
    }

    @Override
    @Transactional
    public Mono<Organization> createOrganization(Organization organization, UUID creatorUserId) {
        log.info("Création d'une nouvelle organisation : {} par l'utilisateur : {}", organization.getShortName(), creatorUserId);
        
        return organizationRepositoryPort.existsByCode(organization.getCode())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalStateException("Une organisation avec le code " + organization.getCode() + " existe déjà"));
                    }
                    
                    if (organization.getId() == null) {
                        organization.setId(UUID.randomUUID());
                    }
                    
                    return organizationRepositoryPort.save(organization);
                })
                .flatMap(savedOrganization -> {
                    if (creatorUserId == null) {
                        return Mono.just(savedOrganization);
                    }
                    
                    return userRepositoryPort.findById(creatorUserId)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Utilisateur créateur non trouvé avec l'ID : " + creatorUserId)))
                            .flatMap(creator -> {
                                UserOrganization userOrganization = new UserOrganization();
                                userOrganization.setId(UUID.randomUUID());
                                userOrganization.setUserId(creator.getId());
                                userOrganization.setOrganizationId(savedOrganization.getId());
                                userOrganization.setRole(OrganizationRole.OWNER);
                                userOrganization.setIsDefault(true);
                                userOrganization.setIsActive(true);
                                userOrganization.setJoinedAt(LocalDateTime.now());
                                
                                return userOrganizationRepositoryPort.save(userOrganization)
                                        .thenReturn(savedOrganization);
                            });
                });
    }

    @Override
    @Transactional
    public Mono<Organization> createOrganization(Organization organization) {
        return createOrganization(organization, null);
    }

    @Override
    @Transactional
    public Mono<Organization> updateOrganization(UUID id, Organization organizationDetails) {
        log.info("Mise à jour de l'organisation avec l'ID : {}", id);
        
        return getOrganizationById(id)
                .map(organization -> {
                    organization.setShortName(organizationDetails.getShortName());
                    organization.setLongName(organizationDetails.getLongName());
                    organization.setDescription(organizationDetails.getDescription());
                    organization.setLogo(organizationDetails.getLogo());
                    organization.setIsActive(organizationDetails.getIsActive());
                    organization.setIsIndividual(organizationDetails.getIsIndividual());
                    organization.setIsPublic(organizationDetails.getIsPublic());
                    organization.setIsBusiness(organizationDetails.getIsBusiness());
                    organization.setCountry(organizationDetails.getCountry());
                    organization.setCity(organizationDetails.getCity());
                    organization.setAddress(organizationDetails.getAddress());
                    organization.setLocalization(organizationDetails.getLocalization());
                    organization.setOpenTime(organizationDetails.getOpenTime());
                    organization.setCloseTime(organizationDetails.getCloseTime());
                    organization.setEmail(organizationDetails.getEmail());
                    organization.setPhone(organizationDetails.getPhone());
                    organization.setWhatsapp(organizationDetails.getWhatsapp());
                    organization.setSocialNetworks(organizationDetails.getSocialNetworks());
                    organization.setCapitalShare(organizationDetails.getCapitalShare());
                    organization.setTaxNumber(organizationDetails.getTaxNumber());
                    return organization;
                })
                .flatMap(organizationRepositoryPort::save);
    }

    @Override
    @Transactional
    public Mono<Void> deleteOrganization(UUID id) {
        log.info("Suppression (soft delete) de l'organisation avec l'ID : {}", id);
        return getOrganizationById(id)
                .flatMap(organization -> {
                    organization.setDeletedAt(LocalDateTime.now());
                    organization.setIsActive(false);
                    return organizationRepositoryPort.save(organization);
                })
                .then();
    }
}
