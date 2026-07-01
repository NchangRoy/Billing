package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelAuthData {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private UUID id;
    private UUID actorId;
    private String nextStep;
    private String mfaToken;
}
