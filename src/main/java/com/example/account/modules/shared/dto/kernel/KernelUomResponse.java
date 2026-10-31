package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/** Mirrors Kernel's "UomView" schema (/api/product-core/units). */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelUomResponse {
    private UUID id;
    private String code;
    private String name;
    private String symbol;
    private String dimension;
    private BigDecimal baseFactor;
    private Boolean base;
}
