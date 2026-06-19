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
public class LigneBackOrder {
    private UUID idProduit;
    private String referenceProduit;
    private String nomProduit;
    private String unite;
    private Double quantiteCommandee;
    private Double quantiteRecue;
    private Double quantiteEnAttente;
    private BigDecimal prixUnitaire;
    private BigDecimal montantTotal;
    private String notes;
}
