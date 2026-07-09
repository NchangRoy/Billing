package com.example.account.modules.tiers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteClientRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String name;
}
