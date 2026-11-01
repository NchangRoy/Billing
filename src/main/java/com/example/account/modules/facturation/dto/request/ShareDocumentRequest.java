package com.example.account.modules.facturation.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShareDocumentRequest {
    private String contactEmail;
    private String contactName;
}
