package com.example.account.modules.facturation.dto.response;

import com.example.account.modules.facturation.dto.enums.SessionStatus;
import com.example.account.modules.facturation.dto.enums.SessionType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SessionResponse {

    private UUID id;
    private UUID salesPointId;
    private UUID organizationId;
    private UUID agencyId;
    private UUID sellerId;

    private SessionType type;
    private SessionStatus status;

    private BigDecimal openingAmount;
    private BigDecimal closingAmount;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;

    private boolean locked;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
