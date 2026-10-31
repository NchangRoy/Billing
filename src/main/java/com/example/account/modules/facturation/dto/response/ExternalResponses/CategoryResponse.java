package com.example.account.modules.facturation.dto.response.ExternalResponses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private UUID id;
    private UUID parentCategoryId;
    private String code;
    private String name;
    private String path;
    private String status;
}
