package com.example.account.modules.facturation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PinLoginRequest {
    @NotNull
    private UUID organizationId;

    @NotBlank
    private String pin;
}
