package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.core.model.entity.OrganizationScoped;
import com.example.account.modules.facturation.domain.model.LigneDevis;
import com.example.account.modules.facturation.model.enums.StatutDevis;
import com.example.account.modules.facturation.model.enums.TypePaiementDevis;
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

@Table("devis")
@Data

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevisPersistenceEntity  {

    @Id
    @Column("id_devis")
    private UUID idDevis;

    @Column("numero_devis")
    private String numeroDevis;

    @Column("date_creation")
    private LocalDateTime dateCreation;

    @Column("date_validite")
    private LocalDateTime dateValidite;

    

    @Column("statut")
    private StatutDevis statut;

    @Column("lignes_devis")
    private List<LigneDevis> lignesDevis;

    @Column("montant_total")
    private BigDecimal montantTotal;

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

    @Column("montant_ht")
    private BigDecimal montantHT;

    @Column("montant_tva")
    private BigDecimal montantTVA;
   
    @Column("montant_ttc")
    private BigDecimal montantTTC;

    @Column("devise")
    private String devise;

    @Column("taux_change")
    @Builder.Default
    private BigDecimal tauxChange = BigDecimal.ONE;

    @Column("conditions_paiement")
    private String conditionsPaiement;

    @Column("notes")
    private String notes;

    @Column("reference_externe")
    private String referenceExterne;

    

    @Column("envoye_par_email")
    @Builder.Default
    private Boolean envoyeParEmail = false;

    @Column("date_envoi_email")
    private LocalDateTime dateEnvoiEmail;

    @Column("date_acceptation")
    private LocalDateTime dateAcceptation;

    @Column("date_refus")
    private LocalDateTime dateRefus;

    @Column("motif_refus")
    private String motifRefus;

    @Column("id_facture_convertie")
    private UUID idFactureConvertie;

    @Column("remise_globale_pourcentage")
    @Builder.Default
    private BigDecimal remiseGlobalePourcentage = BigDecimal.ZERO;

    @Column("remise_globale_montant")
    @Builder.Default
    private BigDecimal remiseGlobaleMontant = BigDecimal.ZERO;

    @Column("validite_offre_jours")
    @Builder.Default
    private Integer validiteOffreJours = 30;

    @Column("apply_vat")
    @Builder.Default
    private Boolean applyVat = true;

    @Column("date_systeme")
    private LocalDateTime dateSysteme;

    @Column("mode_reglement")
    private TypePaiementDevis modeReglement;

    @Column("nos_ref")
    private String nosRef;

    @Column("vos_ref")
    private String vosRef;

    @Column("nbre_echeance")
    private Integer nbreEcheance;

    @Column("referal_client_id")
    private UUID referalClientId;

    @Column("final_amount")
    private BigDecimal finalAmount;

    @Column("created_by")
    private UUID createdBy;


    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
      
    @Column("organization_id")
    private UUID organizationId;

    @Column("agency_id")
    private UUID agencyId;

    @Version
    @Column("version")
    @Builder.Default
    private Long version = 0L;
}