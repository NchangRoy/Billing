package com.example.account.modules.shared.dto.kernel;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ApiResponseListThirdPartyResponse {
    private Boolean success;
    private List<ThirdPartyResponse> data;
    private String message;
    private String errorCode;
    private String timestamp;
}
