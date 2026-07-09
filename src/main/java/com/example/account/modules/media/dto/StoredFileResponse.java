package com.example.account.modules.media.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class StoredFileResponse {
    private UUID id;
    private UUID organizationId;
    private UUID uploadedByUserId;
    private String fileName;
    private String contentType;
    private Long size;
    private String documentType;
    private String analysisStatus;
    private String analysisReason;
}
