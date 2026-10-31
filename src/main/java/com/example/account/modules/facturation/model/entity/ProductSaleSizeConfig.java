package com.example.account.modules.facturation.model.entity;

import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Per-product sale config — image, per-size pricing/negotiability, and VAT
 * applicability — for a Kernel product-core product. product-core itself has
 * no equivalent concept (its "variants" are a generic sku/name thing, no
 * price/image/VAT field anywhere in the new product schemas), so this stays
 * Billing's own local config, joined onto the Kernel-fetched product by
 * productId at read time.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("product_sale_size_configs")
public class ProductSaleSizeConfig implements Persistable<UUID> {

    @Id
    private UUID id;

    @Transient
    @Builder.Default
    private boolean newEntity = true;

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @Column("product_id")
    private UUID productId;

    @Column("organization_id")
    private UUID organizationId;

    @Column("photo")
    private String photo;

    /** The generated numbering code (category/unit/VAT/date/sequence) — distinct from Kernel's
     * globalCode ("reference"); this is the human-facing identifier shown on the products table. */
    @Column("product_number")
    private String productNumber;

    @Column("vat_applicable")
    @Builder.Default
    private boolean vatApplicable = true;

    @Column("sale_sizes")
    private List<ProductResponse.SaleSize> saleSizes;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
