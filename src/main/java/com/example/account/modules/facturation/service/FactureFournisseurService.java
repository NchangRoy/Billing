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

@Service
@RequiredArgsConstructor
@Slf4j
public class FactureFournisseurService {
    
    private final FactureFournisseurRepository factureFournisseurRepository;
    private final FactureFournisseurMapper factureFournisseurMapper;
    private final R2dbcEntityTemplate entityTemplate;

    @Transactional
    public Mono<FactureFournisseurResponse> createFacture(FactureFournisseurCreateRequest dto) {
        log.info("Création d'une nouvelle facture fournisseur");
        FactureFournisseur factureFournisseur = factureFournisseurMapper.toEntity(dto);
        if (factureFournisseur.getIdFactureFournisseur() == null) {
            factureFournisseur.setIdFactureFournisseur(UUID.randomUUID());
        }
        return entityTemplate.insert(factureFournisseur)
                .map(factureFournisseurMapper::toDto);
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
}