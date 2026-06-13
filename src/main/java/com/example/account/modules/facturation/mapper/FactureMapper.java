package com.example.account.modules.facturation.mapper;

import com.example.account.modules.facturation.dto.request.FactureCreateRequest;
import com.example.account.modules.facturation.dto.request.FactureUpdateRequest;
import com.example.account.modules.facturation.dto.response.FactureResponse;
import com.example.account.modules.facturation.domain.model.Facture;
import com.example.account.modules.tiers.mapper.ClientMapper;
import com.example.account.modules.core.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Context;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {LigneFactureMapper.class, ClientMapper.class, PaiementMapper.class}
)
public interface FactureMapper extends BaseMapper<Facture, FactureCreateRequest, FactureUpdateRequest, FactureResponse> {

   

   
    @Mapping(target = "idFacture", ignore = true)
   
    Facture toEntity(FactureCreateRequest createRequest);

    @Mapping(target = "idFacture", ignore = true)
    @Mapping(target = "numeroFacture", ignore = true)
    @Mapping(target = "montantTotal", ignore = true)
    @Mapping(target = "montantRestant", ignore = true)
    @Mapping(target = "nomClient", ignore = true)
    @Mapping(target = "adresseClient", ignore = true)
    @Mapping(target = "emailClient", ignore = true)
    @Mapping(target = "telephoneClient", ignore = true)
    @Mapping(target = "montantHT", ignore = true)
    @Mapping(target = "montantTVA", ignore = true)
    @Mapping(target = "montantTTC", ignore = true)
    @Mapping(target = "pdfPath", ignore = true)
    @Mapping(target = "envoyeParEmail", ignore = true)
    @Mapping(target = "dateEnvoiEmail", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "validatedBy", ignore = true)
    @Mapping(target = "validatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(FactureUpdateRequest updateRequest, @MappingTarget Facture facture);


    void updateEntityFromRequest(FactureCreateRequest request,@MappingTarget Facture facture);


  
    FactureResponse toResponse(Facture facture);

    List<FactureResponse> toResponseList(List<Facture> factures);

 
    

   
}