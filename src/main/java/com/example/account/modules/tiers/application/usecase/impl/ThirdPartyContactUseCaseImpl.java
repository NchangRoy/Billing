package com.example.account.modules.tiers.application.usecase.impl;

import com.example.account.modules.tiers.domain.port.input.ThirdPartyContactUseCase;
import com.example.account.modules.tiers.domain.port.output.ThirdPartyContactServicePort;
import com.example.account.modules.tiers.dto.AddThirdPartyContactRequest;
import com.example.account.modules.tiers.dto.ThirdPartyContactResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThirdPartyContactUseCaseImpl implements ThirdPartyContactUseCase {

    private final ThirdPartyContactServicePort thirdPartyContactServicePort;

    @Override
    public Flux<ThirdPartyContactResponse> getContacts(UUID thirdPartyId) {
        return thirdPartyContactServicePort.getContacts(thirdPartyId);
    }

    @Override
    public Mono<ThirdPartyContactResponse> addContact(UUID thirdPartyId, AddThirdPartyContactRequest request) {
        return thirdPartyContactServicePort.addContact(thirdPartyId, request);
    }

    @Override
    public Mono<UUID> resolveActorId(UUID thirdPartyId) {
        return thirdPartyContactServicePort.resolveActorId(thirdPartyId);
    }
}
