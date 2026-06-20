package com.example.account.modules.facturation.service.ExternalServices.entity;

import com.example.account.modules.facturation.service.ExternalServices.entity.enums.SaleSizeType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleSizePromotion {

    private SaleSizeType saleSize;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal promotionalPrice;

    private BigDecimal discountPercentage;

    private Boolean active;
}