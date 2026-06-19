package com.example.account.modules.facturation.mapper;

import com.example.account.modules.core.mapper.BaseMapper;
import com.example.account.modules.facturation.domain.model.BackOrder;
import com.example.account.modules.facturation.dto.request.BackOrderRequest;
import com.example.account.modules.facturation.dto.response.BackOrderResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface BackOrderMapper extends BaseMapper<BackOrder, BackOrderRequest, BackOrderRequest, BackOrderResponse> {

    @Override
    @Mapping(target = "idBackOrder", ignore = true)
    BackOrder toEntity(BackOrderRequest request);

    @Override
    BackOrderResponse toResponse(BackOrder entity);

    @Override
    @Mapping(target = "idBackOrder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    void updateEntityFromRequest(BackOrderRequest request, @MappingTarget BackOrder entity);

    List<BackOrderResponse> toResponseList(List<BackOrder> entities);
}
