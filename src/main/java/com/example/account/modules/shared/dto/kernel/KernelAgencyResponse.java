package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelAgencyResponse {
    private UUID id;
    private UUID organizationId;
    private String code;
    private String name;
    private String location;
    private String city;
    private String country;
    private String phone;
    private String email;
    private String agencyType;
    private Boolean isHeadquarter;
    private Boolean active;
}
