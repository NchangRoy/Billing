package com.example.account.modules.facturation.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UIPermissions {

    private UUID id;
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
