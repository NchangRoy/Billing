package com.example.account.modules.facturation.dto.response;


import com.example.account.modules.facturation.model.enums.StatutFacture;
import com.example.account.modules.facturation.model.enums.TypePaiementFacture;
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
public class FactureResponse {

    private String idFacture; // Changed to String to match 'FACT-2026-001'
    private String numeroFacture;
    
    // Using LocalDateTime for ISO consistency with the frontend strings
    private LocalDateTime dateFacturation;
    private LocalDateTime dateEcheance;
    private LocalDateTime dateSysteme;
    
    private String type;
    private StatutFacture etat; // Map to the UpdatedFactureResponse.etat enum
    
    private String idClient; // Changed to String to match 'c001'
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;
    
    // Financial fields (aligned with mock logic)
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal montantTotal;
    private BigDecimal montantRestant;
    private BigDecimal finalAmount;
    
    private Boolean applyVat;
    private String devise;
    private BigDecimal tauxChange;
    
    private TypePaiementFacture modeReglement;
    private String conditionsPaiement;
    private Integer nbreEcheance;
    
    private String nosRef;
    private String vosRef;
    private String referenceCommande;
    private String idDevisOrigine; // Reference to source quote

    // JSON-based line items
    private List<LigneFactureResponse> lignesFacture;

    private String notes;
    private String pdfPath;
    private Boolean envoyeParEmail;
    private LocalDateTime dateEnvoiEmail;
    
    private UUID referalClientId;
    private UUID organizationId;
    private UUID agencyId;
    
    private BigDecimal remiseGlobalePourcentage;
    private BigDecimal remiseGlobaleMontant;
    
    // User context & Audit fields
    private UUID createdBy;
    private String createdByUsername;
    private UUID validatedBy;
    private String validatedByUsername;
    private LocalDateTime validatedAt;
    
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
}