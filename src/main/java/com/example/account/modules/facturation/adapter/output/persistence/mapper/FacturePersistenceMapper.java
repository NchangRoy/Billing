package com.example.account.modules.facturation.adapter.output.persistence.mapper;

import com.example.account.modules.facturation.adapter.output.persistence.FacturePersistenceEntity;
import com.example.account.modules.facturation.domain.model.Facture;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FacturePersistenceMapper {
    Facture toDomain(FacturePersistenceEntity entity);
    FacturePersistenceEntity toEntity(Facture domain);
}
