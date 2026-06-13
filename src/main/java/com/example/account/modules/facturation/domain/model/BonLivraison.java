package com.example.account.modules.facturation.domain.model;

import com.example.account.modules.facturation.model.enums.StatutBonLivraison;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonLivraison {

    private UUID idBonLivraison;
    private String numeroLivraison;
    private UUID idClient;
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;
    private List<LigneBonLivraison> lignesBonLivraison;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private LocalDateTime dateLivraison;
    private StatutBonLivraison statut;
    private String notes;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dateSysteme;
    private UUID organizationId;
}
