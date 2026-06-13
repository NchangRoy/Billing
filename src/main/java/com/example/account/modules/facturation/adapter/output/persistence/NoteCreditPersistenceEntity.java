package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.domain.model.LigneNoteCredit;
import com.example.account.modules.facturation.model.enums.ModeReglementNoteCredit;
import com.example.account.modules.facturation.model.enums.StatutNoteCredit;
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

@Table("notes_credit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteCreditPersistenceEntity {

    @Id
    @Column("id_note_credit")
    private UUID idNoteCredit;

    @Column("numero_note_credit")
    private String numeroNoteCredit;

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

    @Column("id_facture_origine")
    private UUID idFactureOrigine;

    @Column("numero_facture_origine")
    private String numeroFactureOrigine;

    @Column("lignes_note_credit")
    private List<LigneNoteCredit> lignesNoteCredit;

    @Column("montant_ht")
    private BigDecimal montantHT;

    @Column("montant_tva")
    private BigDecimal montantTVA;

    @Column("montant_ttc")
    private BigDecimal montantTTC;

    @Column("montant_total")
    private BigDecimal montantTotal;

    @Column("date_emission")
    private LocalDateTime dateEmission;

    @Column("date_systeme")
    private LocalDateTime dateSysteme;

    @Column("mode_reglement")
    private ModeReglementNoteCredit modeReglement;

    @Column("statut")
    private StatutNoteCredit statut;

    @Column("motif")
    private String motif;

    @Column("notes")
    private String notes;

    @Column("devise")
    private String devise;

    @Column("pdf_path")
    private String pdfPath;

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
}
