package com.example.account.modules.facturation.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Which seller is the account manager for a given supplier/producer. One
 * active assignment per (fournisseurId, organizationId) — mirrors CustomerAssignment.
 */
@Table("producer_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProducerAssignment {

    @Id
    @Column("id_assignment")
    private UUID idAssignment;

    @Column("organization_id")
    private UUID organizationId;

    @Column("fournisseur_id")
    private UUID fournisseurId;

    @Column("seller_id")
    private UUID sellerId;

    @Column("seller_name")
    private String sellerName;

    @Column("assigned_at")
    private LocalDateTime assignedAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
