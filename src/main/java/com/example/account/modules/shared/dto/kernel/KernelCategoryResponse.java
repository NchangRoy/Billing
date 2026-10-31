package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Mirrors Kernel's "CategoryView" schema (/api/product-core/classification/categories). */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelCategoryResponse {
    private UUID id;
    private UUID parentCategoryId;
    private String code;
    private String name;
    private String path;
    private String status;
}
