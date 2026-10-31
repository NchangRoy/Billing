package com.example.account.modules.facturation.dto.response.ExternalResponses;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateSellerResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String role;
    /** Quick-login PIN for the POS terminal. */
    private String pin;
}
