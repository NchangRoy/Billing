package com.example.account.modules.shared.dto.kernel;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponseThirdPartyResponse {
    private Boolean success;
    private ThirdPartyResponse data;
    private String message;
    private String errorCode;
    private String timestamp;
}
