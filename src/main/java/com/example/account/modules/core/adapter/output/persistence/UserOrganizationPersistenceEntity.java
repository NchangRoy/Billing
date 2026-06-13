package com.example.account.modules.core.adapter.output.persistence;

import com.example.account.modules.core.model.enums.OrganizationRole;
import com.example.account.modules.facturation.model.enums.SaleSize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Table("user_organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizationPersistenceEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("organization_id")
    private UUID organizationId;

    @Column("role")
    private OrganizationRole role = OrganizationRole.MEMBER;

    @Column("is_default")
    private Boolean isDefault = false;

    @Column("is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column("joined_at")
    private LocalDateTime joinedAt;

    @Column("left_at")
    private LocalDateTime leftAt;

    @Column("permitted_sale_sizes")
    private String permittedSaleSizes;

    @Column("agency")
    private String agency;

    @Column("sale_point")
    private String salePoint;

    @Transient
    public boolean isActiveMembership() {
        return isActive && leftAt == null;
    }
}
