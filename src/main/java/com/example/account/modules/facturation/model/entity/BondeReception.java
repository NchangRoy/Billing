package com.example.account.modules.facturation.model.entity;

import com.example.account.modules.core.model.entity.OrganizationScoped;
import com.example.account.modules.facturation.model.entity.Lines.LineBonReception;
import com.example.account.modules.facturation.model.enums.StatusBonReception;
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

@Table("bons_reception")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BondeReception {

    @Id
    @Column("id_bon_reception")
    private UUID idBonReception;

    @Column("numero_reception")
    private String numeroReception;

    @Column("id_fournisseur")
    private UUID idFournisseur;

    @Column("nom_fournisseur")
    private String nomFournisseur;

    @Column("lines")
    private List<LineBonReception> lines;

  

    @Column("date_reception")
    private LocalDateTime dateReception;

    @Column("statut")
    private StatusBonReception statut;

    @Column("notes")
    private String notes;

    @Column("created_by")
    private UUID createdBy;

  

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("date_systeme")
    private LocalDateTime dateSysteme;

    @Column("numero_bon_achat")
    private String numeroBonAchat;
    @Column("idBonAchat")
    private UUID idBonAchat;

    @Column("agence_de_transport")
    private String agenceDeTransport;

    @Column("organization_id")
    private UUID organizationId;

    @Column("agency_id")
    private UUID agencyId;
}