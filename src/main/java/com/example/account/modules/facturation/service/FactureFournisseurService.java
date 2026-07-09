package com.example.account.modules.facturation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.account.modules.facturation.dto.request.FactureFournisseurCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureFournisseurResponse;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import com.example.account.modules.facturation.mapper.FactureFournisseurMapper;
import com.example.account.modules.facturation.model.entity.FactureFournisseur;
import com.example.account.modules.facturation.repository.FactureFournisseurRepository;
import com.example.account.modules.facturation.dto.request.AssignDocPermissionRequest;
import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactureFournisseurService {
    
    private final FactureFournisseurRepository factureFournisseurRepository;
    private final FactureFournisseurMapper factureFournisseurMapper;
    private final R2dbcEntityTemplate entityTemplate;
    private final DocPermissionService docPermissionService;

    private <T> Mono<T> grantOwnerPermission(UUID sellerId, UUID docId, T response) {
        if (sellerId == null || docId == null) return Mono.just(response);
        AssignDocPermissionRequest request = new AssignDocPermissionRequest();
        request.setSellerId(sellerId);
        request.setDocId(docId);
        request.setDocType(DocType.FACTURE_FOURNISSEUR);
        request.setPermission(DocPermissionLevel.OWNER);
        return docPermissionService.grant(request)
                .thenReturn(response)
                .onErrorResume(e -> {
                    log.error("Failed to grant owner doc-permission for facture fournisseur {}: {}", docId, e.getMessage());
                    return Mono.just(response);
                });
    }

    @Transactional
    public Mono<FactureFournisseurResponse> createFacture(FactureFournisseurCreateRequest dto) {
        log.info("Création d'une nouvelle facture fournisseur");
        FactureFournisseur factureFournisseur = factureFournisseurMapper.toEntity(dto);
        if (factureFournisseur.getIdFactureFournisseur() == null) {
            factureFournisseur.setIdFactureFournisseur(UUID.randomUUID());
        }
        return entityTemplate.insert(factureFournisseur)
                .flatMap(saved -> grantOwnerPermission(saved.getCreatedBy(), saved.getIdFactureFournisseur(), factureFournisseurMapper.toDto(saved)));
    }

    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getAllFactures() {
        log.info("Récupération de toutes les factures fournisseur");
        return factureFournisseurRepository.findAll()
                .map(factureFournisseurMapper::toDto);
    }

    @Transactional
    public Mono<FactureFournisseurResponse> updateFacture(UUID id, FactureFournisseurCreateRequest dto) {
        log.info("Mise à jour de la facture fournisseur: {}", id);
        return factureFournisseurRepository.findById(id)
                .switchIfEmpty(Mono.error(new Exception("Facture Fournisseur does not exists")))
                .flatMap(factureFournisseur -> {
                    factureFournisseurMapper.updateEntityFromRequest(dto, factureFournisseur);
                    return factureFournisseurRepository.save(factureFournisseur);
                })
                .map(factureFournisseurMapper::toDto);
    }

    @Transactional
    public Mono<Void> deleteFacture(UUID id) {
        log.info("Suppression de la facture fournisseur: {}", id);
        return factureFournisseurRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Facture fournisseur non trouvée: " + id));
                    }
                    return factureFournisseurRepository.deleteById(id);
                });
    }

    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getByOrganizationId(UUID organizationId) {
        return factureFournisseurRepository.findByOrganizationId(organizationId).map(factureFournisseurMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getByAgencyId(UUID agencyId) {
        return factureFournisseurRepository.findByAgencyId(agencyId).map(factureFournisseurMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getBySellerId(UUID sellerId) {
        return docPermissionService.findBySellerAndDocType(sellerId, DocType.FACTURE_FOURNISSEUR)
                .flatMap(permission -> factureFournisseurRepository.findById(permission.getDocId())
                        .map(entity -> {
                            FactureFournisseurResponse response = factureFournisseurMapper.toDto(entity);
                            response.setDocPermission(docPermissionService.toResponse(permission));
                            return response;
                        }));
    }
}