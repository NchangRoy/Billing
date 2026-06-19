package com.example.account.modules.facturation.dto.request;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UIPermissionsRequest {

    private UUID organizationId;
    private UUID agencyId;
    private UUID sellerId;

    // Sales Management
    private boolean salesQuotations;
    private boolean salesProformaInvoices;
    private boolean salesSalesOrders;
    private boolean salesInvoices;
    private boolean salesDeliveryNotes;
    private boolean salesCreditNotes;
    private boolean salesStoreCredit;
    private boolean salesBackOrders;

    // Purchasing & Logistics
    private boolean purchasingPurchaseOrders;
    private boolean purchasingGoodsRns;
    private boolean purchasingSupplierInvoice;

    // Journals
    private boolean journalsQuotations;
}
