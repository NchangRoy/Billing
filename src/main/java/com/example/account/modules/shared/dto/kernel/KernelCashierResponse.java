package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelCashierResponse {
    private UUID id;
    private UUID organizationId;
    private UUID agencyId;
    private String email;
    private String fullName;
    private String kind;
    private Boolean active;
    private Instant createdAt;
}
