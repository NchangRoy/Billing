package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.entity.Lines.LineFactureFournisseur;
import com.example.account.modules.facturation.model.enums.ModeReglement;
import com.example.account.modules.facturation.model.enums.StatutFactureFournisseur;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureFournisseurCreateRequest {
    private String numeroFacture;
    private UUID idFournisseur;
    private String nomFournisseur;
    private String adresseFournisseur;
    private String emailFournisseur;
    private String telephoneFournisseur;
    private List<LineFactureFournisseur> lines;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal montantTotal;
    private ModeReglement modeReglement;
    private BigDecimal nbreEcheance;
    private BigDecimal montantRestant;
    private LocalDateTime dateFacture;
    private LocalDateTime dateEcheance;
    private StatutFactureFournisseur statut;
    private Boolean applyVat;
    private String devise;
    private String notes;
    private String pdfPath;
    private UUID createdBy;
    private UUID idBonReception;
    private String numeroBonReception;
    private LocalDateTime dateSysteme;
    private UUID organizationId;
    private UUID agencyId;
}