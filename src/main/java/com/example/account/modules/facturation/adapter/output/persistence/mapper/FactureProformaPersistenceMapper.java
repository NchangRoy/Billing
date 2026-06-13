package com.example.account.modules.facturation.adapter.output.persistence.mapper;

import com.example.account.modules.facturation.adapter.output.persistence.FactureProformaPersistenceEntity;
import com.example.account.modules.facturation.domain.model.FactureProforma;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FactureProformaPersistenceMapper {
    FactureProforma toDomain(FactureProformaPersistenceEntity entity);
    FactureProformaPersistenceEntity toEntity(FactureProforma domain);
}
