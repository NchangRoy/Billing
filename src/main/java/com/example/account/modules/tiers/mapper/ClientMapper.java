package com.example.account.modules.tiers.mapper;

import com.example.account.modules.core.mapper.BaseMapper;
import com.example.account.modules.tiers.dto.ClientCreateRequest;
import com.example.account.modules.tiers.dto.ClientUpdateRequest;
import com.example.account.modules.tiers.dto.ClientResponse;
import com.example.account.modules.tiers.domain.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
         unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ClientMapper extends BaseMapper<Client, ClientCreateRequest, ClientUpdateRequest, ClientResponse> {

  
    @Mapping(target = "soldeCourant", constant = "0.0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Client toEntity(ClientCreateRequest createRequest);

    @Mapping(target = "idClient", ignore = true)
    @Mapping(target = "soldeCourant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ClientUpdateRequest updateRequest, @MappingTarget Client client);

    ClientResponse toResponse(Client client);

    List<ClientResponse> toResponseList(List<Client> clients);
}