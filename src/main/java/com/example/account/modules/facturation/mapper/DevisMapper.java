package com.example.account.modules.facturation.mapper;

import com.example.account.modules.facturation.dto.request.DevisCreateRequest;
import com.example.account.modules.facturation.dto.response.DevisResponse;
import com.example.account.modules.facturation.domain.model.Devis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DevisMapper {

    @Mapping(target = "idDevis", ignore = true)
    Devis toDomain(DevisCreateRequest request);

    DevisResponse toResponse(Devis devis);

    List<DevisResponse> toResponseList(List<Devis> devisList);

    void updateDomainFromRequest(DevisCreateRequest request, @MappingTarget Devis devis);
}