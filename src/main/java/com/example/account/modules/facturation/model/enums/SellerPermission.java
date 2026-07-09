package com.example.account.modules.facturation.model.enums;

/**
 * Matches sales-core's seller.domain.enums.Permission exactly (POST/GET seller auth
 * "Permissions" field) — distinct from core.model.enums.Permission, which is
 * Billing's own organization-level RBAC permission set.
 */
public enum SellerPermission {
    NEGOTIATE_PRICE,
    APPLY_DISCOUNT,
    OVERRIDE_PRICE,
    APPROVE_DOCUMENT
}
