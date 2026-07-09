package com.example.account.modules.facturation.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Which seller is the account manager for a given customer. One active
 * assignment per (clientId, organizationId) — assigning again just replaces it.
 */
@Table("customer_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAssignment {

    @Id
    @Column("id_assignment")
    private UUID idAssignment;

    @Column("organization_id")
    private UUID organizationId;

    @Column("client_id")
    private UUID clientId;

    @Column("seller_id")
    private UUID sellerId;

    @Column("seller_name")
    private String sellerName;

    @Column("assigned_at")
    private LocalDateTime assignedAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
