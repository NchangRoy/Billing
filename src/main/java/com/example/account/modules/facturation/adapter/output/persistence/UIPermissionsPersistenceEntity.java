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

    // Sales Management
    @Column("sales_quotations")
    private boolean salesQuotations;

    @Column("sales_proforma_invoices")
    private boolean salesProformaInvoices;

    @Column("sales_sales_orders")
    private boolean salesSalesOrders;

    @Column("sales_invoices")
    private boolean salesInvoices;

    @Column("sales_delivery_notes")
    private boolean salesDeliveryNotes;

    @Column("sales_credit_notes")
    private boolean salesCreditNotes;

    @Column("sales_store_credit")
    private boolean salesStoreCredit;

    @Column("sales_back_orders")
    private boolean salesBackOrders;

    // Purchasing & Logistics
    @Column("purchasing_purchase_orders")
    private boolean purchasingPurchaseOrders;

    @Column("purchasing_goods_rns")
    private boolean purchasingGoodsRns;

    @Column("purchasing_supplier_invoice")
    private boolean purchasingSupplierInvoice;

    // Journals
    @Column("journals_quotations")
    private boolean journalsQuotations;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
