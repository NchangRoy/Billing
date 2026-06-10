package com.example.account.modules.facturation.service.ExternalServices.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortalPermissions {
    private Boolean canView;
    private Boolean canModify;
    private Boolean canReject;
    private Boolean canAccept;
}
