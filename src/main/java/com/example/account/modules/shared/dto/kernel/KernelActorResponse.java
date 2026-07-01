package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelActorResponse {
    private UUID id;
    private UUID actorId;
    private String name;
    private String code;
    private String type;
    private String role;
    private Boolean isActive;
    private Boolean isVerified;
    private Instant createdAt;
}
