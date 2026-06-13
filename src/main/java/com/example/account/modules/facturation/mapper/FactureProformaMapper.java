package com.example.account.modules.facturation.mapper;

import com.example.account.modules.facturation.dto.request.ProformaInvoiceRequest;
import com.example.account.modules.facturation.dto.response.ProformaInvoiceResponse;
import com.example.account.modules.facturation.domain.model.FactureProforma;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FactureProformaMapper {

    @Mapping(target = "idFactureProforma", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lignesFactureProforma", source = "lignes")
    FactureProforma toEntity(ProformaInvoiceRequest request);

    
    ProformaInvoiceResponse toResponse(FactureProforma entity);

    List<ProformaInvoiceResponse> toResponseList(List<FactureProforma> entities);
    void updateProformaFromDTO(ProformaInvoiceRequest reuqest,@MappingTarget FactureProforma proforma);
}
