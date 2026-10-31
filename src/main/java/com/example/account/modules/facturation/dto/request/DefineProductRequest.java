package com.example.account.modules.facturation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DefineProductRequest {

    @NotBlank
    private String globalCode;

    @NotBlank
    private String name;

    /** TANGIBLE or INTANGIBLE. */
    @NotBlank
    private String nature;

    /** RAW_MATERIAL, SEMI_FINISHED, or FINISHED. */
    private String articleKind;

    @NotNull
    private UUID defaultUomId;

    private String shortDescription;
}
