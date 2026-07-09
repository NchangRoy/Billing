package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Mirrors Kernel's product-catalog ProductResponse field-for-field (see the "ProductResponse"
 * schema in Kernel's OpenAPI spec) — several fields here used to have made-up names (active,
 * category, stockQuantity/availableQuantity/reservedQuantity, photoUri) that don't exist on
 * Kernel's actual payload, so they always silently deserialized to null/defaults. This DTO is
 * only used for the Kernel catalog read path; it is unrelated to the local reservation-tracking
 * Product entity/ProductRepository used by the stock-reservation websocket flow.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelProductResponse {
    private UUID id;
    private UUID organizationId;
    private String sku;
    private String name;
    private String familyCode;
    private String categoryCode;
    private String variantLabel;
    private String barcode;
    private String description;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private BigDecimal unitPrice;
    private String currency;
    private String status;
    private BigDecimal cost;
    private String photo;
    private String uom;
    private Double quantity;
    private List<ProductResponse.SaleSize> allowedSaleSizes;
    private String createdAt;
    private String updatedAt;
}
