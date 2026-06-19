package com.example.account.modules.facturation.dto.response;

import com.example.account.modules.facturation.model.enums.ModeReglement;
import com.example.account.modules.facturation.model.enums.StatutProforma;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProformaInvoiceResponse {

    // ✅ renamed to match entity
    private UUID idFactureProforma;

    private String numeroProformaInvoice;
    private LocalDateTime dateCreation;
    private String type;
    private StatutProforma statut;
    private BigDecimal montantTotal;

    private UUID idClient;
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;

    // ✅ renamed to match entity
    private List<LigneProformaResponse> lignesFactureProforma;

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

    private Boolean applyVat;
    private LocalDate dateSysteme;
    private ModeReglement modeReglement;

    private String nosRef;
    private String vosRef;
    private Integer nbreEcheance;
    private UUID referalClientId;

    private BigDecimal finalAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UUID organizationId;
    private UUID agencyId;
    private UUID createdBy;
}