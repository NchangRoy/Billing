package com.example.account.modules.facturation.model.entity;

import com.example.account.modules.facturation.model.entity.Lines.LineFactureFournisseur;
import com.example.account.modules.facturation.model.enums.ModeReglement;
import com.example.account.modules.facturation.model.enums.StatutFactureFournisseur;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table("factures_fournisseur")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactureFournisseur {

    @Id
    @Column("id_facture_fournisseur")
    private UUID idFactureFournisseur;

    @Column("numero_facture")
    private String numeroFacture;

    @Column("id_fournisseur")
    private UUID idFournisseur;

    @Column("nom_fournisseur")
    private String nomFournisseur;

    // Fixed: Matches 'adresse_supplier' found in your \d output
    @Column("adresse_supplier") 
    private String adresseFournisseur;

    @Column("email_fournisseur")
    private String emailFournisseur;

    @Column("telephone_fournisseur")
    private String telephoneFournisseur;

    @Column("lines")
    private List<LineFactureFournisseur> lines;

    @Column("montant_ht")
    private BigDecimal montantHT;

    @Column("montant_tva")
    private BigDecimal montantTVA;

    @Column("montant_ttc")
    private BigDecimal montantTTC;

    @Column("montant_total")
    private BigDecimal montantTotal;

    @Column("mode_reglement")
    private ModeReglement modeReglement;

    @Column("nbre_echeance")
    private BigDecimal nbreEcheance;

    @Column("montant_restant")
    private BigDecimal montantRestant;

    @Column("date_facture")
    private LocalDateTime dateFacture;

    @Column("date_echeance")
    private LocalDateTime dateEcheance;

    @Column("statut")
    private StatutFactureFournisseur statut;

    @Column("apply_vat")
    private Boolean applyVat;

    @Column("devise")
    private String devise;

    @Column("notes")
    private String notes;

    @Column("pdf_path")
    private String pdfPath;

    @Column("created_by")
    private UUID createdBy;

    @Column("id_bon_reception")
    private UUID idBonReception;

    // Adjusted: Renamed to camelCase for standard Java practices
    @Column("numero_bon_reception")
    private String numeroBonReception;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("date_systeme")
    private LocalDateTime dateSysteme;

    @Column("organization_id")
    private UUID organizationId;

    @Column("agency_id")
    private UUID agencyId;
}