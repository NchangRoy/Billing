package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignDocPermissionRequest {
    @NotNull
    private UUID sellerId;
    @NotNull
    private UUID docId;
    @NotNull
    private DocType docType;
    @NotNull
    private DocPermissionLevel permission;

    // Share-flow only (email notification) — supplied directly by the frontend
    // from its already-loaded seller list/session, avoiding a backend lookup.
    private String recipientEmail;
    private String recipientName;
    private String sharedByName;
    private String docLabel;
}
