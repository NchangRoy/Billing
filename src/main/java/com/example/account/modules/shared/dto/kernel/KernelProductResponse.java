package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Mirrors Kernel's "SellableProduct" schema field-for-field (/api/product-core/sellable-products,
 * see Kernel's OpenAPI spec) — this used to mirror "ProductView" from the organization-agnostic
 * /api/product-core/products instead, which has no price/cost/quantity/barcode/image field at
 * all, so every product in the app always showed those as null/zero regardless of what Kernel
 * actually had on file. SellableProduct is the per-organization listing with that data.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelProductResponse {
    private UUID productId;
    private UUID organizationId;
    private String globalCode;
    private String name;
    private String shortDescription;
    private String variantName;
    private String barcode;
    private String familyCode;
    private String categoryCode;
    private BigDecimal unitPrice;
    private String currency;
    private BigDecimal cost;
    private Double quantity;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private String imageUri;
    private String uomCode;
    private List<ProductResponse.SaleSize> allowedSaleSizes;
    private String lifecycleStatus;
    private String createdAt;
    private String updatedAt;
}
