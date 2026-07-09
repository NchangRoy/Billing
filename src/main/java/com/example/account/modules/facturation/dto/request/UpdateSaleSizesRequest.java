package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateSaleSizesRequest {

    @NotNull
    private List<ProductResponse.SaleSize> allowedSaleSizes;
}
