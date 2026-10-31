package com.example.account.modules.facturation.dto.response.ExternalResponses;

import com.example.account.modules.facturation.model.enums.SaleSize;
import com.example.account.modules.facturation.model.enums.SellerPermission;
import com.example.account.modules.facturation.model.enums.SellerRole;
import com.example.account.modules.settings.dto.SettingResponse;
import com.example.account.modules.shared.dto.kernel.KernelDiscoveredContext;
import com.example.account.modules.shared.dto.kernel.KernelOrganizationResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Mirrors sales-core's SellerAuthResponse exactly (POST /api/sellers/auth/login,
 * POST /api/sellers/auth/change-password), including its non-idiomatic "Id"/
 * "Permissions" JSON casing — this is the frontend's contract, so it's a drop-in
 * match rather than Billing's own shape.
 */
@Data
@NoArgsConstructor
public class SellerAuthResponse {

    // JWT returned by sales-core's local seller auth — the frontend must send
    // this as Authorization: Bearer {accessToken}
    private String accessToken;

    // Kernel's refresh token (and its expiry), carried only between
    // AuthServiceAdapter and AuthUseCaseImpl so the latter can cache it for
    // PIN login to later mint a fresh accessToken from — never meant to
    // reach the frontend.
    @JsonIgnore
    private String refreshToken;
    @JsonIgnore
    private String refreshExpiresAt;

    @JsonProperty("Id")
    private UUID id;

    private String username;
    private String email;
    private SellerRole role;
    private String agency;
    private String salePoint;

    @JsonProperty("Permissions")
    private List<SellerPermission> permissions;

    private List<SaleSize> permittedSaleSizes;

    // Organization
    private UUID organizationId;
    private String organizationName;
    private String organizationLogoUri;
    private String organizationEmail;
    private String taxNumber;

    // Agency
    private UUID agencyId;
    private String agencyEmail;
    private String agencyPhone;
    private String agencyCity;
    private String agencyAddress;

    // Sales Point
    private UUID salesPointId;
    private String salesPointAddress;

    private Instant createdAt;

    // Whether the seller is still on a temporary password and must change it
    // before doing anything else. See sales-core's local seller auth.
    private Boolean mustChangePassword;

    private SellerUIPermissionsResponse uiPermissions;

    // Organization-wide settings (logo) and per-document-type numbering configuration,
    // so the frontend has everything it needs to render/compose documents right after login.
    private SettingResponse organizationSettings;
    private List<SettingResponse> documentNumberingSettings;

    // Try Out only: set when the Kernel account belongs to more than one
    // organization and no organizationId was supplied to disambiguate. Every
    // other field above is left null in that case — the frontend must show
    // an org picker and resubmit with the chosen organizationId.
    private Boolean requiresOrganizationSelection;
    private List<KernelOrganizationResponse> availableOrganizations;

    // Try Out only: set once the organization is resolved but no existing
    // seller or subscription was found for it — organizationId/organizationName
    // above are already populated, the frontend just needs to show a plan
    // picker and resubmit LoginRequest.planType to complete provisioning.
    private Boolean requiresPlanSelection;

    // Set when Kernel's discover-contexts finds this account in more than
    // one tenant that actually has organizations (a bare PLATFORM/no-org
    // context never counts). Every other field above is left null — the
    // frontend must show a tenant picker and resubmit with the chosen
    // tenantId, same shape as the organization-selection step one level
    // below this one.
    private Boolean requiresTenantSelection;
    private List<KernelDiscoveredContext> availableTenants;

    // Set when Kernel requires MFA to complete this login (now the case for
    // every account). Every other field is left null — the frontend must
    // prompt for the emailed OTP code and resubmit via /api/auth/login/mfa
    // with this mfaToken + the code (and organizationId/tryOut if known).
    private Boolean mfaRequired;
    private String mfaToken;
    private String mfaChannel;
}
