package com.example.account.modules.settings.domain;

import com.example.account.modules.facturation.model.enums.TypeNumerotation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One row per (organizationId, typeNumerotation) configures how numbers are composed for that
 * document type. A row with typeNumerotation == null is the organization-wide row and only its
 * "uri" (the company logo) is meaningful.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("settings")
public class Setting {

    /** R2DBC identity — auto-increment surrogate key, distinct from the UUID business key below.
     * (Spring Data R2DBC's save() treats a non-null @Id as an UPDATE, so a pre-populated UUID PK
     * would always fail as "row does not exist"; see Product entity for the same pattern.) */
    @Id
    @Column("row_id")
    private Long rowId;

    private UUID id;

    @Column("organization_id")
    private UUID organizationId;

    @Column("type_numerotation")
    private TypeNumerotation typeNumerotation;

    /** Company logo URL when typeNumerotation is null; unused otherwise. */
    private String uri;

    @Column("include_org_code")
    private Boolean includeOrgCode;

    @Column("org_code")
    private String orgCode;

    @Column("include_branch_code")
    private Boolean includeBranchCode;

    @Column("branch_code")
    private String branchCode;

    @Column("include_tva")
    private Boolean includeTva;

    @Column("include_date")
    private Boolean includeDate;

    @Column("random_seq_4")
    private Boolean randomSeq4;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
