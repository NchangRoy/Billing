package com.example.account.modules.shared.dto.kernel;

import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerUIPermissionsResponse;
import com.example.account.modules.facturation.model.enums.SaleSize;
import com.example.account.modules.facturation.model.enums.SellerPermission;
import com.example.account.modules.facturation.model.enums.SellerRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Matches sales-core's SellerAuthResponse (POST /api/sellers/auth/login),
 * including its non-idiomatic "Id"/"Permissions" JSON casing.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesCoreSellerAuthResponse {

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

    private UUID organizationId;
    private String organizationName;
    private String organizationLogoUri;
    private String organizationEmail;
    private String taxNumber;

    private UUID agencyId;
    private String agencyEmail;
    private String agencyPhone;
    private String agencyCity;
    private String agencyAddress;

    private UUID salesPointId;
    private String salesPointAddress;

    private String createdAt;
    private Boolean mustChangePassword;
    private SellerUIPermissionsResponse uiPermissions;
}
