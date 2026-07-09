package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Only the fields Billing's "Try Out" flow actually needs from Kernel's login response. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelLoginResponse {
    private UUID id;
    private String username;
    private String email;
    private String accessToken;
}
