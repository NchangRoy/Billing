package com.example.account.modules.facturation.adapter.output.persistence;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Billing's own authoritative store for per-seller UI permissions. Used to proxy
 * Kernel's sales-core ui-permissions endpoint, but Kernel's schema has no room for
 * groups added after the fact (Quotation Proposals, Product Settings), so this table
 * is now the single source of truth end-to-end — one row per seller.
 */
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

    @Column("section_sales_management")
    private boolean sectionSalesManagement;

    @Column("sales_quotations")
    private boolean salesQuotations;

    @Column("sales_quotation_proposals")
    private boolean salesQuotationProposals;

    @Column("sales_proforma_invoice")
    private boolean salesProformaInvoice;

    @Column("sales_sales_orders")
    private boolean salesSalesOrders;

    @Column("sales_invoices")
    private boolean salesInvoices;

    @Column("sales_delivery_note")
    private boolean salesDeliveryNote;

    @Column("sales_credit_notes")
    private boolean salesCreditNotes;

    @Column("sales_back_orders")
    private boolean salesBackOrders;

    @Column("section_purchasing_logistics")
    private boolean sectionPurchasingLogistics;

    @Column("purchasing_purchase_order")
    private boolean purchasingPurchaseOrder;

    @Column("purchasing_goods_receipt_note")
    private boolean purchasingGoodsReceiptNote;

    @Column("purchasing_supplier_invoice")
    private boolean purchasingSupplierInvoice;

    @Column("section_accounting_journals")
    private boolean sectionAccountingJournals;

    @Column("journals_quotation")
    private boolean journalsQuotation;

    @Column("journals_sale_order")
    private boolean journalsSaleOrder;

    @Column("journals_purchase_order")
    private boolean journalsPurchaseOrder;

    @Column("journals_client_invoice")
    private boolean journalsClientInvoice;

    @Column("journals_supplier_invoice")
    private boolean journalsSupplierInvoice;

    @Column("section_organization")
    private boolean sectionOrganization;

    @Column("organization_agencies")
    private boolean organizationAgencies;

    @Column("organization_sellers")
    private boolean organizationSellers;

    @Column("organization_customers")
    private boolean organizationCustomers;

    @Column("organization_suppliers")
    private boolean organizationSuppliers;

    @Column("organization_sale_points")
    private boolean organizationSalePoints;

    @Column("organization_sessions")
    private boolean organizationSessions;

    @Column("organization_products")
    private boolean organizationProducts;

    @Column("section_settings")
    private boolean sectionSettings;

    @Column("settings_preferences")
    private boolean settingsPreferences;

    @Column("section_product_settings")
    private boolean sectionProductSettings;

    @Column("product_settings_uom")
    private boolean productSettingsUom;

    @Column("product_settings_categories")
    private boolean productSettingsCategories;

    @Column("product_settings_products")
    private boolean productSettingsProducts;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
