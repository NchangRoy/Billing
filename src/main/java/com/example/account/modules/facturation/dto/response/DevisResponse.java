package com.example.account.modules.facturation.dto.response;

import com.example.account.modules.facturation.model.enums.StatutDevis;
import com.example.account.modules.facturation.model.enums.TypePaiementDevis;
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
public class DevisResponse {

    private UUID idDevis;
    private String numeroDevis;
    
    // Using LocalDateTime for ISO 8601 compatibility with TS strings
    private LocalDateTime dateCreation;
    private LocalDateTime dateValidite;
    
    private String type;
    private StatutDevis statut;
    private BigDecimal montantTotal;
    
    // Changed to String to support identifiers like 'c001' from mock
    private String idClient;
    
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;

    // References the POJO used for JSON storage
    private List<LigneDevisResponse> lignesDevis;

    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private String devise;
    private BigDecimal tauxChange;
    private String conditionsPaiement;
    private String notes;
    private String referenceExterne;
    private String pdfPath;
    private Boolean envoyeParEmail;
    private LocalDateTime dateEnvoiEmail;
    private LocalDateTime dateAcceptation;
    private LocalDateTime dateRefus;
    private String motifRefus;
    private UUID idFactureConvertie;
    private BigDecimal remiseGlobalePourcentage;
    private BigDecimal remiseGlobaleMontant;
    private Integer validiteOffreJours;
    
    // --- Added fields from UpdatedDevisResponse ---
    
    private Boolean applyVat;
    private LocalDateTime dateSysteme;
    private TypePaiementDevis modeReglement;
    private String nosRef;
    private String vosRef;
    private Integer nbreEcheance;
    private String referalClientId;
    private BigDecimal finalAmount;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID organizationId;
    private UUID agencyId;
    private UUID createdBy;
    private DocPermissionResponse docPermission;
}