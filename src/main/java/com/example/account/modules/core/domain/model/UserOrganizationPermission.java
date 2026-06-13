package com.example.account.modules.core.domain.model;

import com.example.account.modules.core.model.enums.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrganizationPermission {

    private UUID id;
    private UUID userOrganizationId;
    private Permission permission;
    private LocalDateTime expiryDate;
    
    @Builder.Default
    private boolean isActive = true;

    public boolean isCurrentlyActive() {
        return isActive && (expiryDate == null || expiryDate.isAfter(LocalDateTime.now()));
    }
}
