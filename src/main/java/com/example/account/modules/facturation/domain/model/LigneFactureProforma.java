package com.example.account.modules.facturation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneFactureProforma {
    private UUID idProduit;
    private String nomProduit;
    private String description;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal montantTotal;
    @Builder.Default
    private BigDecimal remisePourcentage = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal remiseMontant = BigDecimal.ZERO;
    private BigDecimal tauxTva;
}
