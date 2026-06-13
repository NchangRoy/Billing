package com.example.account.modules.facturation.adapter.output.persistence.mapper;

import com.example.account.modules.facturation.domain.model.Journal;
import com.example.account.modules.facturation.adapter.output.persistence.JournalPersistenceEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JournalPersistenceMapper {
    Journal toDomain(JournalPersistenceEntity entity);
    JournalPersistenceEntity toEntity(Journal domain);
}
