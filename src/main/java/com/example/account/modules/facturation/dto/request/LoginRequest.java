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

    // Which Kernel tenant this account's login belongs to, once Kernel's
    // discover-contexts has found the account in more than one. Omitted on
    // the first call; the frontend resubmits it after the user picks a
    // tenant from SellerAuthResponse.availableTenants.
    private UUID tenantId;

    // Try Out only: which plan to start the organization on, once
    // ensureSellerProvisioned has found no existing seller or subscription
    // for it and asked for one via SellerAuthResponse.requiresPlanSelection.
    // Omitted on the first call.
    private String planType;
}
