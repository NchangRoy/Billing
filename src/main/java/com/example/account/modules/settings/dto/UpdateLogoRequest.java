package com.example.account.modules.settings.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateLogoRequest {

    @NotBlank
    private String uri;
}
