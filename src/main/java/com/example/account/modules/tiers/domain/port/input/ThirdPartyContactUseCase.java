package com.example.account.modules.tiers.domain.port.input;

import com.example.account.modules.tiers.dto.AddThirdPartyContactRequest;
import com.example.account.modules.tiers.dto.ThirdPartyContactResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ThirdPartyContactUseCase {
    Flux<ThirdPartyContactResponse> getContacts(UUID thirdPartyId);

    Mono<ThirdPartyContactResponse> addContact(UUID thirdPartyId, AddThirdPartyContactRequest request);

    Mono<UUID> resolveActorId(UUID thirdPartyId);
}
