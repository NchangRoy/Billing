package com.example.account.modules.facturation.dto.response.ExternalResponses;

import com.example.account.modules.facturation.model.enums.SaleSize;
import com.example.account.modules.facturation.model.enums.SellerPermission;
import com.example.account.modules.facturation.model.enums.SellerRole;
import com.example.account.modules.settings.dto.SettingResponse;
import com.example.account.modules.shared.dto.kernel.KernelOrganizationResponse;
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

    @JsonProperty("Id")
    private UUID id;

    private String username;
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
}
