package com.example.account.modules.core.adapter.output.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public abstract class OrganizationScopedPersistenceEntity {

    @Column("organization_id")
    private UUID organizationId;

    @Column("agency_id")
    private UUID agencyId;
}
