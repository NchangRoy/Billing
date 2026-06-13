package com.example.account.modules.facturation.domain.model;

import com.example.account.modules.core.domain.model.OrganizationScoped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Taxes extends OrganizationScoped {

    private UUID idTaxe;
    private String nomTaxe;
    private BigDecimal calculTaxe;
    @Builder.Default
    private Boolean actif = true;
    private String typeTaxe;
    private String porteTaxe;
    private BigDecimal montant;
    private String positionFiscale;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
