package com.example.account.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class SendSellerInvitationEmailRequest {

    private UUID organizationId;

    @NotBlank
    private String email;

    private String firstName;
    private String pin;
    private String agency;
    private String role;
}
