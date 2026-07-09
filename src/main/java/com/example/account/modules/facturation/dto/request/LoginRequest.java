package com.example.account.modules.facturation.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;

    // Try Out only: which Kernel organization to complete the login for, once
    // the account has been found to belong to more than one. Omitted on the
    // first call; the frontend resubmits it after the user picks an org from
    // the list returned in SellerAuthResponse.availableOrganizations.
    private UUID organizationId;
}
