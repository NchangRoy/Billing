package com.example.account.modules.facturation.mapper;

import com.example.account.modules.facturation.dto.request.PaiementCreateRequest;
import com.example.account.modules.facturation.dto.request.PaiementUpdateRequest;
import com.example.account.modules.facturation.dto.response.PaiementResponse;
import com.example.account.modules.facturation.domain.model.Paiement;

import com.example.account.modules.core.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaiementMapper extends BaseMapper<Paiement, PaiementCreateRequest, PaiementUpdateRequest, PaiementResponse> {

  
    @Mapping(target = "createdAt", expression = "java(getCurrentTime())")
    @Mapping(target = "updatedAt", expression = "java(getCurrentTime())")
    Paiement toEntity(PaiementCreateRequest createRequest);

    @Mapping(target = "idPaiement", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(PaiementUpdateRequest updateRequest, @MappingTarget Paiement paiement);

    PaiementResponse toResponse(Paiement paiement);

    List<PaiementResponse> toResponseList(List<Paiement> paiements);
}