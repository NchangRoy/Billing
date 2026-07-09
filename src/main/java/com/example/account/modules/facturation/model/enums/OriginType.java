package com.example.account.modules.facturation.model.enums;

/**
 * Distinguishes an invoice created at a POS terminal register (tied to a
 * cash-register session, see sales-core's sessions table) from one created
 * through the regular back-office/web sales flow.
 */
public enum OriginType {
    POS,
    SALES
}
