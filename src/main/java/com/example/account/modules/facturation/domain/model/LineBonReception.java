package com.example.account.modules.facturation.domain.model;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineBonReception {
    private UUID productId;
    private String description;
    private String uom;
    private Double orderedQuantity;
    private Double receivedQuantity;
    private Double acceptedQuantity;
    private Double rejectedQuantity;
    private Double shortQuantity;
    private Double damagedQuantity;
    private Double excessQuantity;
    private Double rate;
    private Double lineAmount;
}
