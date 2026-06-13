package com.example.account.modules.core.adapter.output.persistence.mapper;

import com.example.account.modules.core.domain.model.UserOrganization;
import com.example.account.modules.core.adapter.output.persistence.UserOrganizationPersistenceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserOrganizationPersistenceMapper {
    UserOrganization toDomain(UserOrganizationPersistenceEntity entity);
    UserOrganizationPersistenceEntity toEntity(UserOrganization domain);
}
