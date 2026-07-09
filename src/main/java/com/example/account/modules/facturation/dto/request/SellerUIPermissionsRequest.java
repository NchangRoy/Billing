package com.example.account.modules.facturation.dto.request;

import lombok.Data;

/**
 * Mirrors sales-core's per-seller UI permissions (POST/GET /api/sellers/{sellerId}/ui-permissions).
 * Distinct from Billing's own local UIPermissionsRequest (see UIPermissionsController /api/v1/ui-permissions),
 * which backs a separate local table and is not synced with sales-core.
 */
@Data
public class SellerUIPermissionsRequest {

    private boolean sectionSalesManagement;
    private boolean salesQuotations;
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
}
