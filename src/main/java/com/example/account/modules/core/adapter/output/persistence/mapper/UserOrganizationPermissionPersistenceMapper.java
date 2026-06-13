package com.example.account.modules.core.adapter.output.persistence.mapper;

import com.example.account.modules.core.domain.model.UserOrganizationPermission;
import com.example.account.modules.core.adapter.output.persistence.UserOrganizationPermissionPersistenceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserOrganizationPermissionPersistenceMapper {
    UserOrganizationPermission toDomain(UserOrganizationPermissionPersistenceEntity entity);
    UserOrganizationPermissionPersistenceEntity toEntity(UserOrganizationPermission domain);
}
