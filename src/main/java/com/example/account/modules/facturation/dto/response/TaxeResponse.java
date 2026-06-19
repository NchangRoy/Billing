package com.example.account.modules.facturation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxeResponse {

    private UUID idTaxe;
    private String nomTaxe;
    private BigDecimal calculTaxe;
    private Boolean actif;
    private String typeTaxe;
    private String porteTaxe;
    private BigDecimal montant;
    private String positionFiscale;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID organizationId;
    private UUID agencyId;
}