package com.example.account.modules.tiers.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddThirdPartyContactRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean favorite;
}
