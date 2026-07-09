package com.example.account.modules.settings.dto;

import com.example.account.modules.facturation.model.enums.TypeNumerotation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingResponse {
    private UUID id;
    private UUID organizationId;
    private TypeNumerotation typeNumerotation;
    private String uri;
    private Boolean includeOrgCode;
    private String orgCode;
    private Boolean includeBranchCode;
    private String branchCode;
    private Boolean includeTva;
    private Boolean includeDate;
    private Boolean randomSeq4;
    /** A live example of what a generated number looks like with the current flags. */
    private String preview;
}
