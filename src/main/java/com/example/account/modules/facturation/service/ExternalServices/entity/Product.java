package com.example.account.modules.facturation.service.ExternalServices.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import org.springframework.data.relational.core.mapping.Column;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@Table("products")
public class Product {
   @Id
    @Column("line_id")
    private Long lineId;
    
    private UUID id;
    
    private String name;
   @Column("product_type") 
    private String type;
    
    @Column("sale_price") // Matches $3 in your error
    private BigDecimal salePrice;
    
    private BigDecimal cost;
    private String category;
    private String reference;
    private String barcode;
    private String photo;

    
    private Boolean active;
    
    @Column("created_at")
    private LocalDate createdAt;
    
    @Column("updated_at")
    private LocalDate updatedAt;
    
    private String uom;

    @Column("allowed_sale_sizes") // Matches $13
    private List<SaleSize> allowedSaleSizes;

    @Column("active_promotions") // Matches $14
    private List<SaleSizePromotion> activePromotions;

    @Column("stock_quantity")
    private Double stockQuantity;

    @Column("available_quantity")
    private Double availableQuantity;

    @Column("reserved_quantity")
    private Double reservedQuantity;

    @Column("organization_id")
    private UUID organizationId;

}