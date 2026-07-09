package com.example.account.modules.facturation.dto.response.ExternalResponses;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class AssignAgencyResponse {
    private UUID sellerId;
    private UUID agencyId;
    private String agency;
    private String agencyEmail;
    private String agencyPhone;
    private String agencyCity;
    private String agencyAddress;
}
