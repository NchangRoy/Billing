package com.example.account.modules.facturation.dto.response;

import com.example.account.modules.facturation.model.entity.Lines.LineBonCommande;
import com.example.account.modules.facturation.model.enums.StatusBonCommande;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonCommandeResponse {

    private UUID idBonCommande;
    private String numeroCommande;

    // --- Client & Billing ---
    private UUID idClient;
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;

    // --- Recipient & Shipping ---
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String recipientCity;

    // --- References ---
    private UUID idDevisOrigine;
    private String numeroDevisOrigine;
    private String referenceExterne;
    private String nosRef;
    private String vosRef;

    // --- Dates ---
    private LocalDateTime dateCommande;
    private LocalDateTime dateSysteme;
    private LocalDateTime dateLivraisonPrevue;

    // --- Items ---
    private List<LineBonCommande> lines;

    // --- Financials ---
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private String devise;
    private BigDecimal tauxChange;
    private Boolean applyVat;

    // --- Logistics & Status ---
    private String transportMethod;
    private UUID idAgency;
    private String modeReglement;
    private String conditionsPaiement;
    private Integer delaiLivraison;
    private String adresseLivraison;
    private StatusBonCommande statut;

    // --- Metadata & Audit ---
    private String notes;
    private UUID createdBy;
    private UUID validatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime validatedAt;
    private UUID organizationId;
    private UUID agencyId;
    private DocPermissionResponse docPermission;
}