package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.enums.OriginType;
import com.example.account.modules.facturation.model.enums.StatutFacture; // Updated to match TS 'etat'
import com.example.account.modules.facturation.model.enums.TypePaiementFacture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureCreateRequest {

    private String numeroFacture;

    @NotNull(message = "La date de facturation est obligatoire")
    private LocalDateTime dateFacturation;

    @NotNull(message = "La date d'échéance est obligatoire")
    private LocalDateTime dateEcheance;

    private LocalDateTime dateSysteme;

    private String type;

    @Builder.Default
    private StatutFacture etat = StatutFacture.BROUILLON; // Corrected spelling and Enum name
    @NotNull(message = "L'ID client est obligatoire")
    private UUID idClient; // Changed to String to match 'c001'

    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;

    @Valid
    private List<LigneFactureCreateRequest> lignesFacture;

    // Totals
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal montantTotal;
    private BigDecimal finalAmount;
    private BigDecimal montantRestant;

    private Boolean applyVat;
    private String devise;

    @Builder.Default
    private BigDecimal tauxChange = BigDecimal.ONE;

    private TypePaiementFacture modeReglement;
    private String conditionsPaiement;
    private Integer nbreEcheance;
    
    private String nosRef;
    private String vosRef;
    private String referenceCommande;
    private UUID idDevisOrigine;

    private String notes;
    private String pdfPath;
    
    @Builder.Default
    private Boolean envoyeParEmail = false;
    
    private LocalDateTime dateEnvoiEmail;

    private BigDecimal remiseGlobalePourcentage;
    private BigDecimal remiseGlobaleMontant;
    
    private UUID  referalClientId;
    private UUID organizationId;
    private UUID agencyId;
    private UUID createdBy;
    private OriginType originType;
    private UUID sessionId;
}