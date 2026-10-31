package com.example.account.modules.facturation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateCategoryRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private UUID parentId;
}
