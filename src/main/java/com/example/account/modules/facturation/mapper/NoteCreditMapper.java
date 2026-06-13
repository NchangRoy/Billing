package com.example.account.modules.facturation.mapper;

import com.example.account.modules.core.mapper.BaseMapper;
import com.example.account.modules.facturation.dto.request.NoteCreditRequest;
import com.example.account.modules.facturation.dto.response.LigneFactureResponse;
import com.example.account.modules.facturation.dto.response.NoteCreditResponse;
import com.example.account.modules.facturation.domain.model.LigneNoteCredit;
import com.example.account.modules.facturation.domain.model.NoteCredit;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface NoteCreditMapper extends BaseMapper<NoteCredit, NoteCreditRequest, NoteCreditRequest, NoteCreditResponse> {

    @Override
    @Mapping(target = "idNoteCredit", ignore = true)
    @Mapping(target = "lignesNoteCredit", source = "lignesNoteCredit")
    NoteCredit toEntity(NoteCreditRequest createRequest);

    @Override
    @Mapping(target = "lignesNoteCredit", source = "lignesNoteCredit")
    NoteCreditResponse toResponse(NoteCredit entity);

    @Override
    @Mapping(target = "idNoteCredit", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    void updateEntityFromRequest(NoteCreditRequest updateRequest, @MappingTarget NoteCredit entity);

    List<NoteCreditResponse> toResponseList(List<NoteCredit> entities);

    // Mapping for lines
    // LigneNoteCredit has proper fields now, no need to remap non-existent fields
    @Mapping(target = "prixUnitaire", source = "prixUnitaire")
    @Mapping(target = "montantTotal", source = "montantTotal")
    @Mapping(target = "idProduit", source = "idProduit")
    LigneFactureResponse toLigneResponse(LigneNoteCredit ligne);

    List<LigneFactureResponse> toLigneResponseList(List<LigneNoteCredit> lignes);
}
