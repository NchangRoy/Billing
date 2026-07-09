package com.example.account.modules.media.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class MediaAssetResponse {
    private UUID id;
    private String targetType;
    private UUID targetId;
    private UUID fileId;
    private String mimeType;
    private Integer position;
    private String altText;
}
