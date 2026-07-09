package com.example.account.modules.facturation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignCustomerRequest {
    @NotNull
    private UUID clientId;
    @NotNull
    private UUID sellerId;
    private String sellerName;
    @NotNull
    private UUID organizationId;
}
