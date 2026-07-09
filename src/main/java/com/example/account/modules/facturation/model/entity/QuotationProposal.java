package com.example.account.modules.facturation.model.entity;

import com.example.account.modules.facturation.model.enums.StatutQuotationProposal;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * A quotation drafted by the CLIENT themselves via the portal (see
 * QuotationProposalService) rather than by a seller — no negotiation or
 * seller-permission checks apply, only the client's own allowed sale sizes.
 * A seller reviews it from the admin "Quotation Proposals" page and
 * accepts/rejects, optionally leaving feedback in `commentary`.
 */
@Table("quotation_proposals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationProposal {

    @Id
    @Column("id_proposal")
    private UUID idProposal;

    @Column("numero_proposal")
    private String numeroProposal;

    @Column("organization_id")
    private UUID organizationId;

    @Column("id_client")
    private UUID idClient;

    @Column("nom_client")
    private String nomClient;

    @Column("email_client")
    private String emailClient;

    @Column("date_creation")
    private LocalDateTime dateCreation;

    @Column("date_validite")
    private LocalDateTime dateValidite;

    @Column("statut")
    private StatutQuotationProposal statut;

    @Column("commentary")
    private String commentary;

    @Column("lignes_proposal")
    private List<LigneQuotationProposal> lignesProposal;

    @Column("montant_ht")
    private BigDecimal montantHT;

    @Column("montant_tva")
    private BigDecimal montantTVA;

    @Column("montant_ttc")
    private BigDecimal montantTTC;

    @Column("apply_vat")
    private Boolean applyVat;

    @Column("devise")
    private String devise;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
