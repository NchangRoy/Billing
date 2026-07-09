package com.example.account.modules.facturation.model.entity;

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
public class LigneQuotationProposal {
    private UUID idProduit;
    private String nomProduit;
    private String saleSize;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal montantTotal;
}
