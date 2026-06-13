package com.example.account.modules.facturation.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneBonLivraison {
    private UUID idProduit;
    private String description;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal montant;
}
