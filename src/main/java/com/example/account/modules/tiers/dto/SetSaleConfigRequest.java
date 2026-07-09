package com.example.account.modules.tiers.dto;

import lombok.Data;

import java.util.List;

@Data
public class SetSaleConfigRequest {
    private List<String> allowedSaleSizes;
    private boolean vatApplicable;
}
