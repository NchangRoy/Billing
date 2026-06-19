package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.enums.StatutDevis;
import com.example.account.modules.facturation.model.enums.TypePaiementDevis;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class DevisCreateRequest {

    @JsonAlias({"devisNumber","numero","numeroDevis","number"})
    private String numeroDevis; // Often auto-generated, but included if client specifies

    @JsonAlias({"creation_date", "creationDate","date_creation"})
    private LocalDateTime dateCreation;

    @JsonAlias({"validity_date", "validityDate", "dateValidite","date_validite"})
    private LocalDateTime dateValidite;

    private String type;

    private StatutDevis statut;

    @NotNull(message = "L'ID client est obligatoire")
    @JsonAlias({"clientId","id_client","idClient"})
    private UUID idClient;

    @JsonAlias({"clientName","nom_client","nomClient"})
    private String nomClient;
    @JsonAlias({"clientAddress","adresse_client","adresseClient"})
    private String adresseClient;
    @JsonAlias({"clientEmail","email_client","emailClient"})
    private String emailClient;
    @JsonAlias({"clientPhone","telephone_client","telephoneClient"})
    private String telephoneClient;

    @Valid
    @JsonAlias({"lines","ligneDevis","lignes","devisLines"})
    private List<LigneDevisCreateRequest> lignesDevis;

    // Financial Summary Fields
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal montantTotal;
    private BigDecimal finalAmount;

    private String devise;

    @Builder.Default
    private BigDecimal tauxChange = BigDecimal.ONE;

    private String conditionsPaiement;
    private String notes;
    private String referenceExterne;

    @PositiveOrZero(message = "La remise globale en pourcentage doit être positive ou nulle")
    private BigDecimal remiseGlobalePourcentage;

    @PositiveOrZero(message = "La remise globale en montant doit être positive ou nulle")
    private BigDecimal remiseGlobaleMontant;

    @Builder.Default
    private Integer validiteOffreJours = 30;

    // --- Added Fields from UpdatedDevisResponse ---

   
    private Boolean applyVat;

    private LocalDateTime dateSysteme;

    private TypePaiementDevis modeReglement;

    private String nosRef;

    private String vosRef;

    private Integer nbreEcheance;

    private UUID referalClientId;
    
    private String pdfPath;
   
    private UUID organizationId;
    private UUID agencyId;
    private UUID createdBy;
    
}