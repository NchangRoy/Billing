package com.example.account.modules.facturation.mapper;

import com.example.account.modules.facturation.dto.request.BonLivraisonRequest;
import com.example.account.modules.facturation.dto.response.BonLivraisonResponse;
import com.example.account.modules.facturation.model.entity.BonLivraison;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {LigneBonLivraisonMapper.class}
)
public interface BonLivraisonMapper {

    @Mapping(target = "idBonLivraison", ignore = true)
    @Mapping(target = "numeroLivraison", source = "numeroBonLivraison")
    @Mapping(target = "adresseClient", source = "adresseClient")
    @Mapping(target = "emailClient", source = "emailClient")
    @Mapping(target = "telephoneClient", source = "telephoneClient")
    @Mapping(target = "dateLivraison", source = "dateLivraison")
    @Mapping(target = "lignesBonLivraison", source = "lignes")
    @Mapping(target = "montantHT", source = "montantHT")
    @Mapping(target = "montantTVA", source = "montantTVA")
    @Mapping(target = "montantTTC", source = "montantTTC")
    @Mapping(target = "notes", source = "notes")
    BonLivraison toEntity(BonLivraisonRequest request);

    @Mapping(target = "numeroBonLivraison", source = "numeroLivraison")
    @Mapping(target = "adresseClient", source = "adresseClient")
    @Mapping(target = "emailClient", source = "emailClient")
    @Mapping(target = "telephoneClient", source = "telephoneClient")
    @Mapping(target = "dateLivraison", source = "dateLivraison")
    @Mapping(target = "lignes", source = "lignesBonLivraison")
    BonLivraisonResponse toResponse(BonLivraison entity);

    @Mapping(target = "lignes", source = "lines")
    List<BonLivraisonResponse> toResponseList(List<BonLivraison> entities);

    @Mapping(target = "idBonLivraison", ignore = true)
    @Mapping(target = "numeroLivraison", source = "numeroBonLivraison")
    @Mapping(target = "idClient", source = "idClient")
    @Mapping(target = "nomClient", source = "nomClient")
    @Mapping(target = "adresseClient", source = "adresseClient")
    @Mapping(target = "emailClient", source = "emailClient")
    @Mapping(target = "telephoneClient", source = "telephoneClient")
    @Mapping(target = "lignesBonLivraison", source = "lignes")
    @Mapping(target = "montantHT", source = "montantHT")
    @Mapping(target = "montantTVA", source = "montantTVA")
    @Mapping(target = "montantTTC", source = "montantTTC")
    @Mapping(target = "dateLivraison", source = "dateLivraison")
    @Mapping(target = "statut", source = "statut")
    @Mapping(target = "notes", source = "notes")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "dateSysteme", source = "dateSysteme")
    @Mapping(target = "organizationId", source = "organizationId")
    @Mapping(target = "agencyId", source = "agencyId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(BonLivraisonRequest request, @MappingTarget BonLivraison object);
}
