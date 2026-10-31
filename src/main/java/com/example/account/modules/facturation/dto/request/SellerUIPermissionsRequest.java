package com.example.account.modules.facturation.dto.request;

import lombok.Data;

/**
 * Per-seller UI permissions, stored locally in Billing's own {@code ui_permissions} table
 * (see {@code SellerUIPermissionsLocalService}). Previously proxied to Kernel's sales-core
 * ui-permissions endpoint, but Kernel's schema has no room for groups added after the fact
 * (Quotation Proposals, Product Settings), so this is now authoritative end-to-end.
 */
@Data
public class SellerUIPermissionsRequest {

    private boolean sectionSalesManagement;
    private boolean salesQuotations;
    private boolean salesQuotationProposals;
    private boolean salesProformaInvoice;
    private boolean salesSalesOrders;
    private boolean salesInvoices;
    private boolean salesDeliveryNote;
    private boolean salesCreditNotes;
    private boolean salesBackOrders;

    private boolean sectionPurchasingLogistics;
    private boolean purchasingPurchaseOrder;
    private boolean purchasingGoodsReceiptNote;
    private boolean purchasingSupplierInvoice;

    private boolean sectionAccountingJournals;
    private boolean journalsQuotation;
    private boolean journalsSaleOrder;
    private boolean journalsPurchaseOrder;
    private boolean journalsClientInvoice;
    private boolean journalsSupplierInvoice;

    private boolean sectionOrganization;
    private boolean organizationAgencies;
    private boolean organizationSellers;
    private boolean organizationCustomers;
    private boolean organizationSuppliers;
    private boolean organizationSalePoints;
    private boolean organizationSessions;
    private boolean organizationProducts;

    private boolean sectionSettings;
    private boolean settingsPreferences;

    private boolean sectionProductSettings;
    private boolean productSettingsUom;
    private boolean productSettingsCategories;
    private boolean productSettingsProducts;
}
