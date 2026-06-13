package com.example.account.modules.facturation.adapter.output.persistence.mapper;

import com.example.account.modules.facturation.adapter.output.persistence.NoteCreditPersistenceEntity;
import com.example.account.modules.facturation.domain.model.NoteCredit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoteCreditPersistenceMapper {
    NoteCredit toDomain(NoteCreditPersistenceEntity entity);
    NoteCreditPersistenceEntity toEntity(NoteCredit domain);
}
