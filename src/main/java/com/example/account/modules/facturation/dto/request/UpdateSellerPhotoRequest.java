package com.example.account.modules.facturation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSellerPhotoRequest {
    @NotBlank
    private String profileImageUrl;
}
