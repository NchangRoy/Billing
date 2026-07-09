package com.example.account.modules.facturation.dto.response.ExternalResponses;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateSellerResponse {
    private UUID id;
    private String username;
    private String role;
    /** Shown only once, at creation time. The seller must change it on first login. */
    private String temporaryPassword;
    /** Quick-login PIN for the POS terminal. */
    private String pin;
}
