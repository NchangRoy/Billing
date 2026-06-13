package com.example.account.modules.core.adapter.output.persistence.mapper;

import com.example.account.modules.core.domain.model.Organization;
import com.example.account.modules.core.adapter.output.persistence.OrganizationPersistenceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizationPersistenceMapper {
    Organization toDomain(OrganizationPersistenceEntity entity);
    OrganizationPersistenceEntity toEntity(Organization domain);
}
