package com.example.account.modules.facturation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.account.modules.facturation.dto.request.FactureFournisseurCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureFournisseurResponse;
import com.example.account.modules.facturation.dto.request.AssignDocPermissionRequest;
import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;
import com.example.account.modules.facturation.domain.port.output.AccountingServicePort;
import com.example.account.modules.facturation.domain.port.output.FactureFournisseurServicePort;

/**
 * FactureFournisseur is now persisted in sales-core; this service is a thin
 * orchestrator on top of FactureFournisseurServicePort (WebClient -> sales-core),
 * keeping only the account-only concerns: doc-permission grants and the
 * permission-aware seller-scoped lookup used by the supplier portal.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FactureFournisseurService {

    private final FactureFournisseurServicePort factureFournisseurServicePort;
    private final DocPermissionService docPermissionService;
    private final AccountingServicePort accountingServicePort;

    public Mono<Void> accountFacture(UUID id) {
        log.info("Comptabilisation de la facture fournisseur: {}", id);
        return accountingServicePort.sendFactureFournisseurData(id)
                .onErrorResume(e -> {
                    log.error("Failed to sync facture fournisseur {} with accounting: {}", id, e.getMessage());
                    return Mono.error(new Exception("Accounting sync failed: " + e.getMessage()));
                });
    }

    public Mono<Void> markAccounted(UUID id) {
        return accountingServicePort.markFactureFournisseurAccounted(id);
    }

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
        return factureFournisseurServicePort.createFacture(dto)
                .flatMap(saved -> grantOwnerPermission(saved.getCreatedBy(), saved.getIdFactureFournisseur(), saved));
    }

    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getAllFactures() {
        log.info("Récupération de toutes les factures fournisseur");
        return factureFournisseurServicePort.getAllFactures();
    }

    @Transactional
    public Mono<FactureFournisseurResponse> updateFacture(UUID id, FactureFournisseurCreateRequest dto) {
        log.info("Mise à jour de la facture fournisseur: {}", id);
        return factureFournisseurServicePort.updateFacture(id, dto);
    }

    @Transactional
    public Mono<Void> deleteFacture(UUID id) {
        log.info("Suppression de la facture fournisseur: {}", id);
        return factureFournisseurServicePort.deleteFacture(id);
    }

    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getByOrganizationId(UUID organizationId) {
        return factureFournisseurServicePort.getByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getByAgencyId(UUID agencyId) {
        return factureFournisseurServicePort.getByAgencyId(agencyId);
    }

    @Transactional(readOnly = true)
    public Flux<FactureFournisseurResponse> getBySellerId(UUID sellerId) {
        return docPermissionService.findBySellerAndDocType(sellerId, DocType.FACTURE_FOURNISSEUR)
                .flatMap(permission -> factureFournisseurServicePort.findById(permission.getDocId())
                        .map(response -> {
                            response.setDocPermission(docPermissionService.toResponse(permission));
                            return response;
                        }));
    }
}
