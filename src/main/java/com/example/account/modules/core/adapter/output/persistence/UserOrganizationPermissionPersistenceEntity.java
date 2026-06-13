package com.example.account.modules.core.adapter.output.persistence;

import com.example.account.modules.core.model.enums.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("user_organization_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrganizationPermissionPersistenceEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("user_organization_id")
    private UUID userOrganizationId;

    @Column("permission")
    private Permission permission;

    @Column("expiry_date")
    private LocalDateTime expiryDate;

    @Column("is_active")
    @Builder.Default
    private boolean isActive = true;

    @Transient
    public boolean isCurrentlyActive() {
        return isActive && (expiryDate == null || expiryDate.isAfter(LocalDateTime.now()));
    }
}
