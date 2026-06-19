package com.example.account.modules.facturation.adapter.output.persistence.mapper;

import com.example.account.modules.facturation.domain.model.Paiement;
import com.example.account.modules.facturation.adapter.output.persistence.PaiementPersistenceEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface PaiementPersistenceMapper {
    Paiement toDomain(PaiementPersistenceEntity entity);
    PaiementPersistenceEntity toEntity(Paiement domain);
}
