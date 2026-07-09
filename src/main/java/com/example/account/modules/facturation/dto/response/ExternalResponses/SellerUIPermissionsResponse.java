package com.example.account.modules.facturation.dto.response.ExternalResponses;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SellerUIPermissionsResponse {

    private UUID id;
    private UUID organizationId;
    private UUID agencyId;
    private UUID sellerId;

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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
