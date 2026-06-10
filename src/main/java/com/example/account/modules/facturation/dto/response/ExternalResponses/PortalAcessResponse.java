package com.example.account.modules.facturation.dto.response.ExternalResponses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortalAcessResponse<T> {
    private T data; // This can now be DevisResponse, InvoiceResponse, etc.
    private Boolean canView;
    private Boolean canModify;
    private Boolean canReject;
    private Boolean canAccept;
}