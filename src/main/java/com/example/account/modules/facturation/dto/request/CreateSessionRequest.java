package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.dto.enums.SessionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateSessionRequest {

    @NotNull
    private SessionType type;

    /** Required when type is POS; omitted when type is SALES. */
    private UUID salesPointId;

    /** Required when type is SALES (no sales point to derive them from); omitted when type is POS. */
    private UUID organizationId;
    private UUID agencyId;

    @NotNull
    private UUID sellerId;

    @NotNull
    private BigDecimal openingAmount;

    /** Defaults to now if omitted. */
    private LocalDateTime startTime;

    /** Optional — a session is normally still open at creation, but callers can pre-set a planned end time. */
    private LocalDateTime endTime;
}
