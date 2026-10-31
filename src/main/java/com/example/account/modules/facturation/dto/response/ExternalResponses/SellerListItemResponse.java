package com.example.account.modules.facturation.dto.response.ExternalResponses;

import com.example.account.modules.facturation.model.enums.SaleSize;
import com.example.account.modules.facturation.model.enums.SellerPermission;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SellerListItemResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String agency;
    private String salePoint;
    private String profileImageUrl;
    private List<SellerPermission> permissions;
    private List<SaleSize> permittedSaleSizes;
    private UUID organizationId;
    private UUID agencyId;
    private UUID salesPointId;
    private Boolean mustChangePassword;
    private String pin;
    /** ACTIVE (added from an existing Kernel employee) or PENDING (invited, hasn't accepted yet). Billing's own local tracking — sales-core has no such concept. */
    private String status;
    private Instant createdAt;
}
