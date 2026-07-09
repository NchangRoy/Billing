package com.example.account.modules.settings.dto;

import lombok.Data;

@Data
public class UpdateSequenceSettingRequest {
    private Boolean includeOrgCode;
    private String orgCode;
    private Boolean includeBranchCode;
    private String branchCode;
    private Boolean includeTva;
    private Boolean includeDate;
    private Boolean randomSeq4;
}
