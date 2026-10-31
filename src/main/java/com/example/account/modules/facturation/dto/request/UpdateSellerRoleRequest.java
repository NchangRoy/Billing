package com.example.account.modules.facturation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSellerRoleRequest {

    /** POS_SELLER, SELLER, AGENCY_MANAGER, or OWNER. */
    @NotBlank
    private String role;
}
