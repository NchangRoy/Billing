package com.example.account.modules.core.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public abstract class OrganizationScoped {
    private UUID organizationId;
    private UUID agencyId;
}
