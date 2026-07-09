package com.example.account.modules.core.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgencyCreateRequest {

    @NotBlank(message = "Agency code is required")
    private String code;

    @NotBlank(message = "Agency name is required")
    private String name;

    private String agencyType;

    @Builder.Default
    private Boolean isHeadquarter = false;

    private String city;

    private String country;
}
