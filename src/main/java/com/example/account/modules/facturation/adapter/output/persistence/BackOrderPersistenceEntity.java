package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.domain.model.LigneBackOrder;
import com.example.account.modules.facturation.model.enums.StatutBackOrder;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table("back_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackOrderPersistenceEntity {

    @Id
    @Column("id_back_order")
    private UUID idBackOrder;

    @Column("numero_back_order")
    private String numeroBackOrder;

    @Column("id_bon_achat")
    private UUID idBonAchat;

    @Column("numero_bon_achat")
    private String numeroBonAchat;

    @Column("id_fournisseur")
    private UUID idFournisseur;

    @Column("nom_fournisseur")
    private String nomFournisseur;

    @Column("lignes")
    private List<LigneBackOrder> lignes;

    @Column("date_creation")
    private LocalDateTime dateCreation;

    @Column("date_livraison_prevue")
    private LocalDateTime dateLivraisonPrevue;

    @Column("date_systeme")
    private LocalDateTime dateSysteme;

    @Column("statut")
    private StatutBackOrder statut;

    @Column("notes")
    private String notes;

    @Column("organization_id")
    private UUID organizationId;

    @Column("agency_id")
    private UUID agencyId;

    @Column("created_by")
    private UUID createdBy;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
