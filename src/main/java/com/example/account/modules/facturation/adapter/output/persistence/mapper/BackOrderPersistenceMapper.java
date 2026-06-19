package com.example.account.modules.facturation.adapter.output.persistence.mapper;

import com.example.account.modules.facturation.adapter.output.persistence.BackOrderPersistenceEntity;
import com.example.account.modules.facturation.domain.model.BackOrder;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface BackOrderPersistenceMapper {
    BackOrder toDomain(BackOrderPersistenceEntity entity);
    BackOrderPersistenceEntity toEntity(BackOrder domain);
}
