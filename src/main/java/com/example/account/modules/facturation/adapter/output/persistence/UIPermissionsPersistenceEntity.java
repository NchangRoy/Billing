package com.example.account.modules.facturation.adapter.output.persistence;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("ui_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UIPermissionsPersistenceEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("organization_id")
    private UUID organizationId;

    @Column("agency_id")
    private UUID agencyId;

    @Column("seller_id")
    private UUID sellerId;

    // Sales
    @Column("sales_quotations")
    private boolean salesQuotations;

    @Column("sales_orders")
    private boolean salesOrders;

    // Inventory
    @Column("inventory_receipts")
    private boolean inventoryReceipts;

    @Column("inventory_delivery_orders")
    private boolean inventoryDeliveryOrders;

    @Column("inventory_transfers")
    private boolean inventoryTransfers;

    @Column("inventory_adjustments")
    private boolean inventoryAdjustments;

    // Accounting
    @Column("accounting_customer_invoices")
    private boolean accountingCustomerInvoices;

    @Column("accounting_vendor_bills")
    private boolean accountingVendorBills;

    @Column("accounting_credit_notes")
    private boolean accountingCreditNotes;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
