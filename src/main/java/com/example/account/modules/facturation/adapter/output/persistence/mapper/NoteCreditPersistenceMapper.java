package com.example.account.modules.facturation.adapter.output.persistence.mapper;

import com.example.account.modules.facturation.adapter.output.persistence.NoteCreditPersistenceEntity;
import com.example.account.modules.facturation.domain.model.NoteCredit;
import org.mapstruct.Mapper;
import org.mapstruct.Builder;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface NoteCreditPersistenceMapper {
    NoteCredit toDomain(NoteCreditPersistenceEntity entity);
    NoteCreditPersistenceEntity toEntity(NoteCredit domain);
}
