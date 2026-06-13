package com.example.account.modules.facturation.domain.model;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineBonCommande {
    private Double quantite;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private Boolean isTaxLine;
    private String idProduit;
    private String nomProduit;
    private BigDecimal prixUnitaire;
    private BigDecimal montantTotal;
    private BigDecimal remisePourcentage;
    private BigDecimal remiseMontant;
}
