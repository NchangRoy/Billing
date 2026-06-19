package com.example.account.modules.facturation.mapper;

import com.example.account.modules.facturation.domain.model.UIPermissions;
import com.example.account.modules.facturation.dto.request.UIPermissionsRequest;
import com.example.account.modules.facturation.dto.response.UIPermissionsResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface UIPermissionsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UIPermissions toEntity(UIPermissionsRequest request);

    UIPermissionsResponse toResponse(UIPermissions entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UIPermissionsRequest request, @MappingTarget UIPermissions entity);
}
