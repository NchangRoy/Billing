package com.example.account.modules.facturation.domain.model;

import com.example.account.modules.facturation.model.enums.StatutFacture;
import com.example.account.modules.facturation.model.enums.TypePaiementFacture;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture {

    private UUID idFacture;
    private String numeroFacture;
    private LocalDateTime dateFacturation;
    private LocalDateTime dateEcheance;
    private LocalDateTime dateSysteme;
    private String type;
    private StatutFacture etat;
    private UUID idClient;
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;
    private List<LigneFacture> lignesFacture;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal montantTotal;
    private BigDecimal montantRestant;
    private BigDecimal finalAmount;
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
    private String idDevisOrigine;
    private UUID referalClientId;
    private String notes;
    private String pdfPath;
    @Builder.Default
    private Boolean envoyeParEmail = false;
    private LocalDateTime dateEnvoiEmail;
    @Builder.Default
    private BigDecimal remiseGlobalePourcentage = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal remiseGlobaleMontant = BigDecimal.ZERO;
    private UUID createdBy;
    private UUID validatedBy;
    private LocalDateTime validatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID organizationId;
    @Builder.Default
    private Long version = 0L;
}
