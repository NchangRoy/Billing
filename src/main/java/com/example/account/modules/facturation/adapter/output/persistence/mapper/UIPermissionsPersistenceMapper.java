package com.example.account.modules.facturation.adapter.output.persistence.mapper;

import com.example.account.modules.facturation.adapter.output.persistence.UIPermissionsPersistenceEntity;
import com.example.account.modules.facturation.domain.model.UIPermissions;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UIPermissionsPersistenceMapper {
    UIPermissions toDomain(UIPermissionsPersistenceEntity entity);
    UIPermissionsPersistenceEntity toEntity(UIPermissions domain);
}
