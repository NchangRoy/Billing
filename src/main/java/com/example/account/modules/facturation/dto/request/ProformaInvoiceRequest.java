package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.enums.ModeReglement;
import com.example.account.modules.facturation.model.enums.StatutProforma;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProformaInvoiceRequest {

    private String numeroProformaInvoice;

    @NotNull(message = "L'ID client est obligatoire")
    private UUID idClient;

    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;

    private List<LigneProformaRequest> lignes;

    private String type;
    private StatutProforma statut;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private String devise;
    private BigDecimal tauxChange;
    private String conditionsPaiement;
    private String notes;
    private String referenceExterne;
    private Boolean applyVat;
    private LocalDate dateSysteme;
    private ModeReglement modeReglement;
    private String nosRef;
    private String vosRef;
    private Integer nbreEcheance;
    private UUID referalClientId;
    private BigDecimal remiseGlobalePourcentage;
    private BigDecimal remiseGlobaleMontant;
    private Integer validiteOffreJours;
    private UUID organizationId;
    private UUID agencyId;
    private UUID createdBy;
}
