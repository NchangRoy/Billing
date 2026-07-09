package com.example.account.modules.facturation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignProducerRequest {
    @NotNull
    private UUID fournisseurId;
    @NotNull
    private UUID sellerId;
    private String sellerName;
    @NotNull
    private UUID organizationId;
}
