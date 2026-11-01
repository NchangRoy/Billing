package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** One entry from actor-core's GET /api/actors/{actorId}/contacts "address book" sub-resource. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelActorContactResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String secondaryEmail;
    private String phoneNumber;
    private boolean isFavorite;
}
