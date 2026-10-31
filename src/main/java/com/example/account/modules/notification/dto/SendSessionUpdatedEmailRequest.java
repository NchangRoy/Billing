package com.example.account.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class SendSessionUpdatedEmailRequest {

    private UUID organizationId;

    @NotBlank
    private String email;

    private String firstName;
    private String agency;
    private String sessionType;
    private String action;
    private String status;
    private BigDecimal openingAmount;
    private BigDecimal closingAmount;
}
