package com.example.account.modules.facturation.domain.model;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneBonAchat {
    private UUID productId;
    private String productCode;
    private String productName;
    private String uom;
    private Integer orderedQuantity;
    private BigDecimal unitPrice;
    private Boolean taxable;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;
}
