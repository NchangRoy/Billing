package com.example.account.modules.facturation.adapter.output.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Facture (client invoice) and FactureFournisseur (supplier invoice) are not
 * local tables — their service ports delegate CRUD to a real /api/factures(-
 * fournisseur) REST API hosted on kernel-core.yowyob.com's own sales-core
 * module, whose schema Billing doesn't own. This is a Billing-side index
 * only: factureId -> the client/fournisseur's Kernel actor id, populated at
 * creation time, so the client portal can find "my invoices" without a
 * cross-service join. Shared by both document types since factureId is a
 * UUID regardless of which one it came from.
 */
@Table("facture_actor_index")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactureActorIndexEntity {

    @Id
    @Column("facture_id")
    private UUID factureId;

    @Column("actor_id")
    private UUID actorId;
}
