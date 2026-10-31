package com.example.account.modules.facturation.dto.response.ExternalResponses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UomResponse {
    private UUID id;
    private String code;
    private String name;
    private String symbol;
    private String dimension;
    private BigDecimal baseFactor;
    private Boolean base;
}
