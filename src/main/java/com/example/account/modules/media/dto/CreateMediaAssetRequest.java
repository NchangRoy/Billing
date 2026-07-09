package com.example.account.modules.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateMediaAssetRequest {

    @NotBlank
    private String targetType;

    @NotNull
    private UUID targetId;

    @NotNull
    private UUID fileId;

    private String mimeType;
    private Integer position;
    private String altText;
}
