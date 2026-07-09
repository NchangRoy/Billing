package com.example.account.modules.facturation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CloseSessionRequest {

    @NotNull
    private BigDecimal closingAmount;
}
