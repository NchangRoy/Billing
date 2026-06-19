package com.example.account.modules.facturation.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UIPermissionsResponse {

    private UUID id;
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
