package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.entity.Lines.LineBonCommande;
import com.example.account.modules.facturation.model.enums.StatusBonCommande;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonCommandeCreateRequest {

    @NotBlank(message = "Le numéro de commande est obligatoire")
    private String numeroCommande;

    // --- Client / Billing Info ---
    @NotNull(message = "L'ID client est obligatoire")
    private UUID idClient;
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;

    // --- Recipient / Shipping Info ---
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
    @NotNull(message = "La date de commande est obligatoire")
    private LocalDateTime dateCommande;
    private LocalDateTime dateSysteme;
    private LocalDateTime dateLivraisonPrevue;

    // --- Lines (Items) ---
    @NotEmpty(message = "La commande doit contenir au moins une ligne")
    private List<LineBonCommande> lines;

    // --- Financial Summary ---
    @NotNull(message = "Le montant TTC est obligatoire")
    @PositiveOrZero
    private BigDecimal montantTTC;
    
    @PositiveOrZero
    private BigDecimal montantHT;
    
    @PositiveOrZero
    private BigDecimal montantTVA;
    
    @Builder.Default
    private String devise = "XAF";
    
    @Builder.Default
    private BigDecimal tauxChange = BigDecimal.ONE;
    
    private Boolean applyVat;

    // --- Logistics & Payment ---
    private String transportMethod;
    private UUID idAgency;
    private String modeReglement;
    private String conditionsPaiement;
    private Integer delaiLivraison;
    private String adresseLivraison;

    // --- Status & Audit ---
    private StatusBonCommande statut;
    private String notes;
    private UUID createdBy;
    private UUID organizationId;
    private UUID agencyId;
}