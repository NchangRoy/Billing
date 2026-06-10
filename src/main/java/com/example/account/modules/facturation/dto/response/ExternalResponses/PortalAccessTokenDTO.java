package com.example.account.modules.facturation.dto.response.ExternalResponses;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.account.modules.facturation.service.ExternalServices.entity.enums.ResourceType;

import lombok.Data;

@Data
public class PortalAccessTokenDTO {
    private String token;
    private ResourceType resourceType;
    private UUID resourceId;
    private boolean used;
    private LocalDateTime expiresAt;
}

