package com.example.account.modules.facturation.model.entity;

import com.example.account.modules.core.model.entity.OrganizationScoped;
import com.example.account.modules.facturation.model.enums.StatutBonLivraison;
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

@Table("bons_livraison")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BonLivraison  {

    @Id
    @Column("id_bon_livraison")
    private UUID idBonLivraison;

    @Column("numero_livraison")
    private String numeroLivraison;

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

    @Column("lignes_bon_livraison")
    private List<LigneBonLivraison> lignesBonLivraison;

    @Column("montant_ht")
    private BigDecimal montantHT;

    @Column("montant_tva")
    private BigDecimal montantTVA;

    @Column("montant_ttc")
    private BigDecimal montantTTC;

    @Column("date_livraison")
    private LocalDateTime dateLivraison;


    @Column("statut")
    private StatutBonLivraison statut;

    @Column("notes")
    private String notes;

    @Column("created_by")
    private UUID createdBy;

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
