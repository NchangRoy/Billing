package com.example.account.modules.facturation.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class QuotationProposalLineRequest {
    private UUID idProduit;
    private String nomProduit;
    private String saleSize;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal montantTotal;
}
