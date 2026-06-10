package com.example.account.modules.facturation.dto.response.ExternalResponses;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccessPortalPermissionsDTO {
    private boolean accept;
    private boolean reject;
    private boolean modify;
}