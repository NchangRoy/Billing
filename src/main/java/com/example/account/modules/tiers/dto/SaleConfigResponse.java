package com.example.account.modules.tiers.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SaleConfigResponse {
    private UUID id;
    private UUID thirdPartyId;
    private UUID organizationId;
    private List<String> allowedSaleSizes;
    private boolean vatApplicable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
