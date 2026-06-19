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

    // Sales
    private boolean salesQuotations;
    private boolean salesOrders;

    // Inventory
    private boolean inventoryReceipts;
    private boolean inventoryDeliveryOrders;
    private boolean inventoryTransfers;
    private boolean inventoryAdjustments;

    // Accounting
    private boolean accountingCustomerInvoices;
    private boolean accountingVendorBills;
    private boolean accountingCreditNotes;
}
