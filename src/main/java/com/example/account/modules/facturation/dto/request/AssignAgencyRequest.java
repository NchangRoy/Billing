package com.example.account.modules.facturation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignAgencyRequest {
    @NotNull
    private UUID agencyId;
}
