package com.example.account.modules.facturation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CustomerAssignmentResponse {
    private UUID idAssignment;
    private UUID organizationId;
    private UUID clientId;
    private UUID sellerId;
    private String sellerName;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
}
