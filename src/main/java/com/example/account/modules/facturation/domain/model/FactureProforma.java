package com.example.account.modules.facturation.domain.model;

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
public class FactureProforma {

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
    private List<LigneFactureProforma> lignesFactureProforma;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private String devise;
    private BigDecimal tauxChange;
    private String conditionsPaiement;
    private String notes;
    private String referenceExterne;
    private String pdfPath;
    @Builder.Default
    private Boolean envoyeParEmail = false;
    private LocalDateTime dateEnvoiEmail;
    private LocalDateTime dateAcceptation;
    private LocalDateTime dateRefus;
    private String motifRefus;
    private UUID idFactureConvertie;
    private BigDecimal remiseGlobalePourcentage;
    private BigDecimal remiseGlobaleMontant;
    private Integer validiteOffreJours;
    @Builder.Default
    private Boolean applyVat = true;
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
