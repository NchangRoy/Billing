package com.example.account.modules.facturation.dto.response;

import com.example.account.modules.facturation.model.entity.LigneBonLivraison;
import com.example.account.modules.facturation.model.enums.StatutBonLivraison;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonLivraisonResponse {
    private UUID idBonLivraison;
    private String numeroBonLivraison;

    private UUID idSaleOrder;
    private String saleOrderNumber;
    private boolean isPartial;

    private UUID idClient;
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;

    private List<LigneBonLivraison> lignes;

    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;

    private LocalDateTime dateLivraison;
   

    private StatutBonLivraison statut;
    private String notes;
     private UUID createdBy;
    private LocalDateTime dateSysteme;
    private LocalDateTime updatedAt;
   

    private UUID organizationId;
    private UUID agencyId;
}
