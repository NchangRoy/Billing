package com.example.account.modules.core.domain.model;

import com.example.account.modules.core.model.enums.OrganizationRole;
import com.example.account.modules.facturation.model.enums.SaleSize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganization {

    private UUID id;
    private UUID userId;
    private UUID organizationId;
    private OrganizationRole role = OrganizationRole.MEMBER;
    private Boolean isDefault = false;
    private Boolean isActive = true;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private String permittedSaleSizes;
    private String agency;
    private String salePoint;

    public boolean isActiveMembership() {
        return isActive && leftAt == null;
    }

    public void setPermittedSaleSizesList(List<SaleSize> saleSizes) {
        if (saleSizes == null || saleSizes.isEmpty()) {
            this.permittedSaleSizes = null;
        } else {
            this.permittedSaleSizes = saleSizes.stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(","));
        }
    }

    public List<SaleSize> getPermittedSaleSizesList() {
        if (permittedSaleSizes == null || permittedSaleSizes.isEmpty()) {
            return List.of();
        }
        return List.of(permittedSaleSizes.split(",")).stream()
                .map(SaleSize::valueOf)
                .collect(Collectors.toList());
    }
}
