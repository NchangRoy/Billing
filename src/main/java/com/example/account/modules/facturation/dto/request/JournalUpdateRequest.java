package com.example.account.modules.facturation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalUpdateRequest {

    private String nomJournal;
    private String type;
    private UUID organizationId;
    private UUID agencyId;
}