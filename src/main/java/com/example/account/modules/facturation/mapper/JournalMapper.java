package com.example.account.modules.facturation.mapper;

import com.example.account.modules.core.mapper.BaseMapper;
import com.example.account.modules.facturation.dto.request.JournalCreateRequest;
import com.example.account.modules.facturation.dto.request.JournalUpdateRequest;
import com.example.account.modules.facturation.dto.response.JournalResponse;
import com.example.account.modules.facturation.domain.model.Journal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface JournalMapper extends BaseMapper<Journal, JournalCreateRequest, JournalUpdateRequest, JournalResponse> {

    @Mapping(target = "createdAt", expression = "java(getCurrentTime())")
    @Mapping(target = "updatedAt", expression = "java(getCurrentTime())")
    Journal toEntity(JournalCreateRequest createRequest);

    @Mapping(target = "idJournal", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(JournalUpdateRequest updateRequest, @MappingTarget Journal journal);

    JournalResponse toResponse(Journal journal);

    List<JournalResponse> toResponseList(List<Journal> journals);
}