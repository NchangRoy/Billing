package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Mirrors Kernel's "ProductView" schema (/api/product-core/products) — the organization's
 * catalog of defined products, with no price/cost/stock (that only exists once a product is
 * provisioned as sellable — see KernelProductResponse/SellableProduct). */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelProductDefinitionResponse {
    private UUID id;
    private String globalCode;
    private String name;
    private String shortDescription;
    private String longDescription;
    private String nature;
    private String articleKind;
    private String lifecycleStatus;
    private UUID defaultUomId;
    private UUID categoryId;
    private String launchDate;
    private String discontinueDate;
}
