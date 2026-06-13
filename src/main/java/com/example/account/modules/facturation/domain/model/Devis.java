package com.example.account.modules.facturation.domain.model;

import com.example.account.modules.facturation.model.enums.StatutDevis;
import com.example.account.modules.facturation.model.enums.TypePaiementDevis;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Devis {

    private UUID idDevis;
    private String numeroDevis;
    private LocalDateTime dateCreation;
    private LocalDateTime dateValidite;
    private StatutDevis statut;
    private List<LigneDevis> lignesDevis;
    private BigDecimal montantTotal;
    private UUID idClient;
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private String devise;
    @Builder.Default
    private BigDecimal tauxChange = BigDecimal.ONE;
    private String conditionsPaiement;
    private String notes;
    private String referenceExterne;
    @Builder.Default
    private Boolean envoyeParEmail = false;
    private LocalDateTime dateEnvoiEmail;
    private LocalDateTime dateAcceptation;
    private LocalDateTime dateRefus;
    private String motifRefus;
    private UUID idFactureConvertie;
    @Builder.Default
    private BigDecimal remiseGlobalePourcentage = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal remiseGlobaleMontant = BigDecimal.ZERO;
    @Builder.Default
    private Integer validiteOffreJours = 30;
    @Builder.Default
    private Boolean applyVat = true;
    private LocalDateTime dateSysteme;
    private TypePaiementDevis modeReglement;
    private String nosRef;
    private String vosRef;
    private Integer nbreEcheance;
    private UUID referalClientId;
    private BigDecimal finalAmount;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID organizationId;
    @Builder.Default
    private Long version = 0L;
}
