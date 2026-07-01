package com.example.account.modules.facturation.dto.response.ExternalResponses;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SellerAuthResponse {

    // JWT returned by the Kernel — the frontend must send this as Authorization: Bearer {accessToken}
    private String accessToken;

    // Seller Identity
    private UUID id;
    private String username;
   
    // Organization
    private UUID organizationId;
    private String organizationName;
    private String organizationLogoUri;
    private String organizationEmail;
    private String taxNumber;

    // Agency
    private UUID agencyId;
    private String agencyName;
    private String agencyEmail;
    private String agencyPhone;
    private String agencyWhatsapp;
    private String agencyCity;
    private String agencyAddress;

    // Sales Point
    private UUID salesPointId;
    private String salesPointName;
    private String salesPointAddress;

    private Instant createdAt;
}
