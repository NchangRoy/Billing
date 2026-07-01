package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelOrganizationResponse {
    private UUID id;
    private String displayName;
    private String legalName;
    private String shortName;
    private String email;
    private String taxNumber;
    private String logoUri;
    private Boolean isActive;
}
