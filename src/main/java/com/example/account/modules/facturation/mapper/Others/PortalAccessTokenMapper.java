package com.example.account.modules.facturation.mapper.Others;

import com.example.account.modules.facturation.dto.response.ExternalResponses.PortalAccessTokenDTO;
import com.example.account.modules.facturation.model.entity.Others.PortalAccessToken;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public abstract class PortalAccessTokenMapper {

    
    public abstract PortalAccessTokenDTO toDto(PortalAccessToken entity);

   
}