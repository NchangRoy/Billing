package com.example.account.modules.facturation.service;

import com.example.account.modules.facturation.adapter.output.persistence.UIPermissionsPersistenceEntity;
import com.example.account.modules.facturation.adapter.output.persistence.UIPermissionsR2dbcRepository;
import com.example.account.modules.facturation.dto.request.SellerUIPermissionsRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerUIPermissionsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authoritative, Billing-local store for per-seller UI permissions — replaces the
 * earlier Kernel-synced version (sales-core's own ui-permissions schema has no room
 * for groups added after the fact, like Quotation Proposals or Product Settings).
 * One row per seller, enforced by a unique constraint (changelog-v1.4).
 */
@Service
@RequiredArgsConstructor
public class SellerUIPermissionsLocalService {

    private final UIPermissionsR2dbcRepository repository;
    private final R2dbcEntityTemplate entityTemplate;

    /** The owner's access can't depend on a configured row (there may never be one). */
    public SellerUIPermissionsResponse fullAccess(UUID sellerId) {
        SellerUIPermissionsResponse r = new SellerUIPermissionsResponse();
        r.setSellerId(sellerId);
        r.setSectionSalesManagement(true);
        r.setSalesQuotations(true);
        r.setSalesQuotationProposals(true);
        r.setSalesProformaInvoice(true);
        r.setSalesSalesOrders(true);
        r.setSalesInvoices(true);
        r.setSalesDeliveryNote(true);
        r.setSalesCreditNotes(true);
        r.setSalesBackOrders(true);
        r.setSectionPurchasingLogistics(true);
        r.setPurchasingPurchaseOrder(true);
        r.setPurchasingGoodsReceiptNote(true);
        r.setPurchasingSupplierInvoice(true);
        r.setSectionAccountingJournals(true);
        r.setJournalsQuotation(true);
        r.setJournalsSaleOrder(true);
        r.setJournalsPurchaseOrder(true);
        r.setJournalsClientInvoice(true);
        r.setJournalsSupplierInvoice(true);
        r.setSectionOrganization(true);
        r.setOrganizationAgencies(true);
        r.setOrganizationSellers(true);
        r.setOrganizationCustomers(true);
        r.setOrganizationSuppliers(true);
        r.setOrganizationSalePoints(true);
        r.setOrganizationSessions(true);
        r.setOrganizationProducts(true);
        r.setSectionSettings(true);
        r.setSettingsPreferences(true);
        r.setSectionProductSettings(true);
        r.setProductSettingsUom(true);
        r.setProductSettingsCategories(true);
        r.setProductSettingsProducts(true);
        return r;
    }

    public Mono<SellerUIPermissionsResponse> get(UUID sellerId) {
        return repository.findFirstBySellerId(sellerId)
                .map(this::toResponse)
                .switchIfEmpty(Mono.just(defaultResponse(sellerId)));
    }

    public Mono<SellerUIPermissionsResponse> set(UUID sellerId, UUID organizationId, UUID agencyId,
                                                  SellerUIPermissionsRequest request) {
        return repository.findFirstBySellerId(sellerId)
                .flatMap(existing -> {
                    applyRequest(existing, request);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    UIPermissionsPersistenceEntity fresh = UIPermissionsPersistenceEntity.builder()
                            .id(UUID.randomUUID())
                            .sellerId(sellerId)
                            .organizationId(organizationId)
                            .agencyId(agencyId)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    applyRequest(fresh, request);
                    // Client-generated id means Spring Data would otherwise guess this
                    // row already exists and attempt an UPDATE — insert explicitly.
                    return entityTemplate.insert(fresh);
                }))
                .map(this::toResponse);
    }

    private void applyRequest(UIPermissionsPersistenceEntity entity, SellerUIPermissionsRequest r) {
        entity.setSectionSalesManagement(r.isSectionSalesManagement());
        entity.setSalesQuotations(r.isSalesQuotations());
        entity.setSalesQuotationProposals(r.isSalesQuotationProposals());
        entity.setSalesProformaInvoice(r.isSalesProformaInvoice());
        entity.setSalesSalesOrders(r.isSalesSalesOrders());
        entity.setSalesInvoices(r.isSalesInvoices());
        entity.setSalesDeliveryNote(r.isSalesDeliveryNote());
        entity.setSalesCreditNotes(r.isSalesCreditNotes());
        entity.setSalesBackOrders(r.isSalesBackOrders());

        entity.setSectionPurchasingLogistics(r.isSectionPurchasingLogistics());
        entity.setPurchasingPurchaseOrder(r.isPurchasingPurchaseOrder());
        entity.setPurchasingGoodsReceiptNote(r.isPurchasingGoodsReceiptNote());
        entity.setPurchasingSupplierInvoice(r.isPurchasingSupplierInvoice());

        entity.setSectionAccountingJournals(r.isSectionAccountingJournals());
        entity.setJournalsQuotation(r.isJournalsQuotation());
        entity.setJournalsSaleOrder(r.isJournalsSaleOrder());
        entity.setJournalsPurchaseOrder(r.isJournalsPurchaseOrder());
        entity.setJournalsClientInvoice(r.isJournalsClientInvoice());
        entity.setJournalsSupplierInvoice(r.isJournalsSupplierInvoice());

        entity.setSectionOrganization(r.isSectionOrganization());
        entity.setOrganizationAgencies(r.isOrganizationAgencies());
        entity.setOrganizationSellers(r.isOrganizationSellers());
        entity.setOrganizationCustomers(r.isOrganizationCustomers());
        entity.setOrganizationSuppliers(r.isOrganizationSuppliers());
        entity.setOrganizationSalePoints(r.isOrganizationSalePoints());
        entity.setOrganizationSessions(r.isOrganizationSessions());
        entity.setOrganizationProducts(r.isOrganizationProducts());

        entity.setSectionSettings(r.isSectionSettings());
        entity.setSettingsPreferences(r.isSettingsPreferences());

        entity.setSectionProductSettings(r.isSectionProductSettings());
        entity.setProductSettingsUom(r.isProductSettingsUom());
        entity.setProductSettingsCategories(r.isProductSettingsCategories());
        entity.setProductSettingsProducts(r.isProductSettingsProducts());
    }

    private SellerUIPermissionsResponse toResponse(UIPermissionsPersistenceEntity e) {
        SellerUIPermissionsResponse r = new SellerUIPermissionsResponse();
        r.setId(e.getId());
        r.setOrganizationId(e.getOrganizationId());
        r.setAgencyId(e.getAgencyId());
        r.setSellerId(e.getSellerId());

        r.setSectionSalesManagement(e.isSectionSalesManagement());
        r.setSalesQuotations(e.isSalesQuotations());
        r.setSalesQuotationProposals(e.isSalesQuotationProposals());
        r.setSalesProformaInvoice(e.isSalesProformaInvoice());
        r.setSalesSalesOrders(e.isSalesSalesOrders());
        r.setSalesInvoices(e.isSalesInvoices());
        r.setSalesDeliveryNote(e.isSalesDeliveryNote());
        r.setSalesCreditNotes(e.isSalesCreditNotes());
        r.setSalesBackOrders(e.isSalesBackOrders());

        r.setSectionPurchasingLogistics(e.isSectionPurchasingLogistics());
        r.setPurchasingPurchaseOrder(e.isPurchasingPurchaseOrder());
        r.setPurchasingGoodsReceiptNote(e.isPurchasingGoodsReceiptNote());
        r.setPurchasingSupplierInvoice(e.isPurchasingSupplierInvoice());

        r.setSectionAccountingJournals(e.isSectionAccountingJournals());
        r.setJournalsQuotation(e.isJournalsQuotation());
        r.setJournalsSaleOrder(e.isJournalsSaleOrder());
        r.setJournalsPurchaseOrder(e.isJournalsPurchaseOrder());
        r.setJournalsClientInvoice(e.isJournalsClientInvoice());
        r.setJournalsSupplierInvoice(e.isJournalsSupplierInvoice());

        r.setSectionOrganization(e.isSectionOrganization());
        r.setOrganizationAgencies(e.isOrganizationAgencies());
        r.setOrganizationSellers(e.isOrganizationSellers());
        r.setOrganizationCustomers(e.isOrganizationCustomers());
        r.setOrganizationSuppliers(e.isOrganizationSuppliers());
        r.setOrganizationSalePoints(e.isOrganizationSalePoints());
        r.setOrganizationSessions(e.isOrganizationSessions());
        r.setOrganizationProducts(e.isOrganizationProducts());

        r.setSectionSettings(e.isSectionSettings());
        r.setSettingsPreferences(e.isSettingsPreferences());

        r.setSectionProductSettings(e.isSectionProductSettings());
        r.setProductSettingsUom(e.isProductSettingsUom());
        r.setProductSettingsCategories(e.isProductSettingsCategories());
        r.setProductSettingsProducts(e.isProductSettingsProducts());

        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }

    private SellerUIPermissionsResponse defaultResponse(UUID sellerId) {
        SellerUIPermissionsResponse r = new SellerUIPermissionsResponse();
        r.setSellerId(sellerId);
        return r;
    }
}
