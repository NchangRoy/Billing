package com.example.account.modules.facturation.adapter.output.persistence;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authoritative local store for a seller's profile (role, permissions, agency,
 * sale point) — replaces what used to be synced to Kernel's sales-core
 * {@code /api/sellers}. The person's actual login identity (email+password,
 * MFA) stays entirely Kernel's; this is matched to that by email once Kernel
 * confirms who they are (see AuthServiceAdapter.enrichWithLocalSeller).
 */
@Table("sellers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerPersistenceEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("email")
    private String email;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("role")
    private String role;

    // r2dbc-postgresql binds text[] columns to native Java arrays, not List<>.
    @Column("permissions")
    private String[] permissions;

    @Column("permitted_sale_sizes")
    private String[] permittedSaleSizes;

    @Column("organization_id")
    private UUID organizationId;

    // Agency is deliberately NOT mapped here — seller_agency_assignments
    // (SellerAgencyAssignmentRepository) is already the working local source
    // of truth for that, unrelated to this migration; this entity doesn't
    // duplicate it.

    @Column("sale_point")
    private String salePoint;

    @Column("sales_point_id")
    private UUID salesPointId;

    @Column("sales_point_address")
    private String salesPointAddress;

    @Column("profile_image_url")
    private String profileImageUrl;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
