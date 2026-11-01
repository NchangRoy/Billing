package com.example.account.modules.notification.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Snapshot of the invoice fields the Telegram batch message needs, captured
 * at enqueue time (when a real caller token exists to fetch them) rather than
 * re-fetched from Kernel at flush time (a background scheduler tick, which
 * has no caller token at all).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingInvoiceSummary {
    private UUID id;
    private String numeroFacture;
    private BigDecimal montant;
    private String devise;
    private String nomClient;
    private String organizationName;
}
