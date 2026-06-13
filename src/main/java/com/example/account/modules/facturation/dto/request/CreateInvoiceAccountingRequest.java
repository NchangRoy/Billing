package com.example.account.modules.facturation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateInvoiceAccountingRequest {
    private UUID invoiceId;
    private String accountingStatus;
}
