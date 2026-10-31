package com.example.account.modules.facturation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {

    /** Used as Kernel's globalCode too — must be unique across the organization's catalog.
     * Computed client-side from the org's product numbering settings (category/unit/VAT),
     * same convention as the other document numbers. */
    @NotBlank
    private String reference;

    @NotBlank
    private String nomProduit;

    private String typeProduit;
    private String categorie;
    private String codeBarre;
    private String uom;
    private String photo;

    private BigDecimal prixVente;
    private BigDecimal cout;
    private String currency;
    private Double quantity;
    private Integer minStockLevel;
    private Integer maxStockLevel;

    /** Billing's own local config (see ProductSaleSizeConfig), not a Kernel field. Defaults to true if omitted. */
    private Boolean vatApplicable;

    /** The generated numbering code (category/unit/VAT/date/sequence), computed client-side —
     * distinct from "reference" (Kernel's globalCode). Stored locally, shown on the products table. */
    private String productNumber;
}
