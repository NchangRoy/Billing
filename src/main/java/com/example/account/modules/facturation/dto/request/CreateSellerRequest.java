package com.example.account.modules.facturation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateSellerRequest {

    @NotBlank
    @Email
    private String email;

    /** Optional for the invite flow (just email + role) — auto-derived from the email if left blank. */
    private String firstName;

    private String lastName;

    /** POS_SELLER, SELLER, AGENCY_MANAGER, or OWNER — defaults to SELLER if not specified. */
    private String role;

    private String agency;
    private String salePoint;

    private List<String> permissions;
    private List<String> permittedSaleSizes;

    @NotNull
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
}
