package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.enums.SaleSize;
import com.example.account.modules.facturation.model.enums.SellerPermission;
import lombok.Data;

import java.util.List;

@Data
public class UpdateSellerPermissionsRequest {
    private List<SellerPermission> permissions;
    private List<SaleSize> permittedSaleSizes;
}
