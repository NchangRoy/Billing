package com.example.account.modules.facturation.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateSessionRequest {
    private BigDecimal openingAmount;
    private Boolean locked;
}
