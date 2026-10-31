package com.example.account.modules.facturation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateUomRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String symbol;

    @NotBlank
    private String dimension;

    private BigDecimal baseFactor;
}
