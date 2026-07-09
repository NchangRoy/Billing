package com.example.account.modules.facturation.dto.response;

import com.example.account.modules.facturation.dto.enums.SalesPointStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SalesPointResponse {

    private UUID id;
    private UUID organizationId;
    private UUID agencyId;
    private String salesPointName;
    private SalesPointStatus status;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
