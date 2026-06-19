package com.example.account.modules.facturation.mapper;

import com.example.account.modules.facturation.dto.request.TaxeCreateRequest;
import com.example.account.modules.facturation.dto.request.TaxeUpdateRequest;
import com.example.account.modules.facturation.dto.response.TaxeResponse;
import com.example.account.modules.facturation.domain.model.Taxes;
import com.example.account.modules.core.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface TaxeMapper extends BaseMapper<Taxes, TaxeCreateRequest, TaxeUpdateRequest, TaxeResponse> {

   
    @Mapping(target = "createdAt", expression = "java(getCurrentTime())")
    @Mapping(target = "updatedAt", expression = "java(getCurrentTime())")
    Taxes toEntity(TaxeCreateRequest createRequest);

    @Mapping(target = "idTaxe", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(TaxeUpdateRequest updateRequest, @MappingTarget Taxes taxe);

    TaxeResponse toResponse(Taxes taxe);

    List<TaxeResponse> toResponseList(List<Taxes> taxes);
}