package com.example.account.modules.facturation.domain.model;

import com.example.account.modules.core.domain.model.OrganizationScoped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Journal extends OrganizationScoped {

    private UUID idJournal;
    private String nomJournal;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
