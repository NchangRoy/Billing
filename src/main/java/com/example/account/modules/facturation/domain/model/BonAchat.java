package com.example.account.modules.facturation.domain.model;

import com.example.account.modules.facturation.model.enums.StatutBonAchat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonAchat {

    private UUID idBonAchat;
    private String numeroBonAchat;
    private UUID idFournisseur;
    private String nomFournisseur;
    private String supplierCode;
    private String supplierEmail;
    private String supplierContact;
    private String supplierAddress;
    private String deliveryName;
    private String deliveryAddress;
    private String deliveryEmail;
    private String deliveryContact;
    private LocalDateTime dateAchat;
    private LocalDateTime dateSysteme;
    private LocalDateTime dateLivraisonPrevue;
    private String transportMethod;
    private String instructionsLivraison;
    private StatutBonAchat statut;
    private List<LigneBonAchat> lignesBonAchat;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private String notes;
    private UUID preparedBy;
    private UUID approvedBy;
    private UUID createdBy;
    private UUID organizationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
