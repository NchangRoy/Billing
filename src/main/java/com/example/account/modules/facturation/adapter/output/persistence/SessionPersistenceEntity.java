package com.example.account.modules.facturation.adapter.output.persistence;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authoritative local store for POS/sales sessions — replaces what used to be
 * proxied to Kernel's sales-core {@code /api/pos-sessions}. sales-core
 * validated sellerId against its own roster, which stopped knowing about any
 * seller created through Billing's local-only seller flow (see
 * SellerPersistenceEntity) and started rejecting every session for them with
 * "Seller not found". Sessions never needed anything else from sales-core, so
 * this drops the dependency entirely.
 * <p>
 * type/status are stored as plain strings (SessionType/SessionStatus name()),
 * same convention as Seller.role — converted at the service boundary.
 */
@Table("sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionPersistenceEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("type")
    private String type;

    @Column("status")
    private String status;

    @Column("sales_point_id")
    private UUID salesPointId;

    @Column("organization_id")
    private UUID organizationId;

    @Column("agency_id")
    private UUID agencyId;

    @Column("seller_id")
    private UUID sellerId;

    @Column("opening_amount")
    private BigDecimal openingAmount;

    @Column("closing_amount")
    private BigDecimal closingAmount;

    @Column("start_time")
    private LocalDateTime startTime;

    @Column("end_time")
    private LocalDateTime endTime;

    @Column("login_time")
    private LocalDateTime loginTime;

    @Column("logout_time")
    private LocalDateTime logoutTime;

    @Column("locked")
    private boolean locked;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
