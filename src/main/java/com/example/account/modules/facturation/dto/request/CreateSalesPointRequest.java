package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.dto.enums.SalesPointStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateSalesPointRequest {

    @NotNull
    private UUID organizationId;

    private UUID agencyId;

    @NotBlank
    private String salesPointName;

    private SalesPointStatus status;

    private String currency;
}
