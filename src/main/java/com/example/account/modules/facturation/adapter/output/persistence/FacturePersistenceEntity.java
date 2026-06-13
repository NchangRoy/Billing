package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.domain.model.LigneFacture;
import com.example.account.modules.facturation.model.enums.StatutFacture;
import com.example.account.modules.facturation.model.enums.TypePaiementFacture;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Facture persistence entity representing an invoice in the database.
 * Line items are stored as JSONB in the lignes_facture column.
 */
@Table("factures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturePersistenceEntity {

    @Id
    @Column("id_facture")
    private UUID idFacture;

    @Column("numero_facture")
    private String numeroFacture;

    @Column("date_facturation")
    private LocalDateTime dateFacturation;

    @Column("date_echeance")
    private LocalDateTime dateEcheance;

    @Column("date_systeme")
    private LocalDateTime dateSysteme;

    @Column("type")
    private String type;

    @Column("etat")
    private StatutFacture etat;

    @Column("id_client")
    private UUID idClient;

    @Column("nom_client")
    private String nomClient;

    @Column("adresse_client")
    private String adresseClient;

    @Column("email_client")
    private String emailClient;

    @Column("telephone_client")
    private String telephoneClient;

    @Column("lignes_facture")
    private List<LigneFacture> lignesFacture;

    @Column("montant_ht")
    private BigDecimal montantHT;

    @Column("montant_tva")
    private BigDecimal montantTVA;

    @Column("montant_ttc")
    private BigDecimal montantTTC;

    @Column("montant_total")
    private BigDecimal montantTotal;

    @Column("montant_restant")
    private BigDecimal montantRestant;

    @Column("final_amount")
    private BigDecimal finalAmount;

    @Column("apply_vat")
    private Boolean applyVat;

    @Column("devise")
    private String devise;

    @Column("taux_change")
    @Builder.Default
    private BigDecimal tauxChange = BigDecimal.ONE;

    @Column("mode_reglement")
    private TypePaiementFacture modeReglement;

    @Column("conditions_paiement")
    private String conditionsPaiement;

    @Column("nbre_echeance")
    private Integer nbreEcheance;

    @Column("nos_ref")
    private String nosRef;

    @Column("vos_ref")
    private String vosRef;

    @Column("reference_commande")
    private String referenceCommande;

    @Column("id_devis_origine")
    private String idDevisOrigine;

    @Column("referal_client_id")
    private UUID referalClientId;

    @Column("notes")
    private String notes;

    @Column("pdf_path")
    private String pdfPath;

    @Column("envoye_par_email")
    @Builder.Default
    private Boolean envoyeParEmail = false;

    @Column("date_envoi_email")
    private LocalDateTime dateEnvoiEmail;

    @Column("remise_globale_pourcentage")
    @Builder.Default
    private BigDecimal remiseGlobalePourcentage = BigDecimal.ZERO;

    @Column("remise_globale_montant")
    @Builder.Default
    private BigDecimal remiseGlobaleMontant = BigDecimal.ZERO;

    @Column("created_by")
    private UUID createdBy;

    @Column("validated_by")
    private UUID validatedBy;

    @Column("validated_at")
    private LocalDateTime validatedAt;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("organization_id")
    private UUID organizationId;

    @Version
    @Column("version")
    @Builder.Default
    private Long version = 0L;
}
