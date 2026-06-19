package com.example.account.modules.facturation.model.entity;

import com.example.account.modules.core.model.entity.OrganizationScoped;
import com.example.account.modules.facturation.model.enums.StatutBonAchat;
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

@Table("bons_achat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BonAchat  {

    @Id
    @Column("id_bon_achat")
    private UUID idBonAchat;

    @Column("numero_bon_achat")
    private String numeroBonAchat;

    // --- Informations Fournisseur ---
    @Column("id_fournisseur")
    private UUID idFournisseur;

    @Column("nom_fournisseur")
    private String nomFournisseur;

    @Column("supplier_code")
    private String supplierCode;

    @Column("supplier_email")
    private String supplierEmail;

    @Column("supplier_contact")
    private String supplierContact;

    @Column("supplier_address")
    private String supplierAddress;

    // --- Informations de Livraison ---
    @Column("delivery_name")
    private String deliveryName;

    @Column("delivery_address")
    private String deliveryAddress;

    @Column("delivery_email")
    private String deliveryEmail;

    @Column("delivery_contact")
    private String deliveryContact;

    // --- Dates & Transport ---
    @Column("date_achat")
    private LocalDateTime dateAchat;

    @Column("date_systeme")
    private LocalDateTime dateSysteme;

    @Column("date_livraison_prevue")
    private LocalDateTime dateLivraisonPrevue;

    @Column("transport_method")
    private String transportMethod;

    @Column("instructions_livraison")
    private String instructionsLivraison;

    @Column("statut")
    private StatutBonAchat statut;

    // --- Lignes et Montants ---
    @Column("lignes_bon_achat")
    private List<LigneBonAchat> lignesBonAchat;

    @Column("montant_ht")
    private BigDecimal montantHT;

    @Column("montant_tva")
    private BigDecimal montantTVA;

    @Column("montant_ttc")
    private BigDecimal montantTTC;

    // --- Audit & Remarques ---
    @Column("notes")
    private String notes;

    @Column("prepared_by")
    private UUID preparedBy;

    @Column("approved_by")
    private UUID approvedBy;

    @Column("created_by")
    private UUID createdBy;

    @Column("organization_id")
    private UUID organizationId;

    @Column("agency_id")
    private UUID agencyId;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}