package com.example.account.modules.facturation.adapter.output.persistence.mapper;

import com.example.account.modules.facturation.adapter.output.persistence.DevisPersistenceEntity;
import com.example.account.modules.facturation.domain.model.Devis;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DevisPersistenceMapper {
    Devis toDomain(DevisPersistenceEntity entity);
    DevisPersistenceEntity toEntity(Devis domain);
}
