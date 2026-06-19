package com.example.account.modules.facturation.adapter.output.persistence.mapper;

import com.example.account.modules.facturation.domain.model.Taxes;
import com.example.account.modules.facturation.adapter.output.persistence.TaxePersistenceEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface TaxePersistenceMapper {
    Taxes toDomain(TaxePersistenceEntity entity);
    TaxePersistenceEntity toEntity(Taxes domain);
}
