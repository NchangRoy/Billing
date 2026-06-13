package com.example.account.modules.tiers.mapper;

import com.example.account.modules.tiers.dto.FournisseurCreateRequest;
import com.example.account.modules.tiers.dto.FournisseurUpdateRequest;
import com.example.account.modules.tiers.dto.FournisseurResponse;
import com.example.account.modules.tiers.domain.model.Fournisseur;
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
public interface FournisseurMapper extends BaseMapper<Fournisseur, FournisseurCreateRequest, FournisseurUpdateRequest, FournisseurResponse> {

 
    @Mapping(target = "soldeCourant", constant = "0.0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Fournisseur toEntity(FournisseurCreateRequest createRequest);

    @Mapping(target = "idFournisseur", ignore = true)
    @Mapping(target = "soldeCourant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(FournisseurUpdateRequest updateRequest, @MappingTarget Fournisseur fournisseur);

    FournisseurResponse toResponse(Fournisseur fournisseur);

    List<FournisseurResponse> toResponseList(List<Fournisseur> fournisseurs);
}