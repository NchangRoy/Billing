package com.example.account.modules.facturation.dto.response;

import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DocPermissionResponse {
    private UUID idPermission;
    private UUID sellerId;
    private UUID docId;
    private DocType docType;
    private DocPermissionLevel permission;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
}
