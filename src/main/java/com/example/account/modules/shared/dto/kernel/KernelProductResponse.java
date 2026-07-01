package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelProductResponse {
    private UUID id;
    private UUID organizationId;
    private String sku;
    private String name;
    private String familyCode;
    private String variantLabel;
    private BigDecimal unitPrice;
    private String currency;
    private Boolean active;
    private String description;
    private String category;
    private String barcode;
    private String uom;
    private Double stockQuantity;
    private Double availableQuantity;
    private Double reservedQuantity;
    private String photoUri;
}
