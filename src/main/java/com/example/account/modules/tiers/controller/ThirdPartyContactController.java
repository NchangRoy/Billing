package com.example.account.modules.tiers.controller;

import com.example.account.modules.tiers.domain.port.input.ThirdPartyContactUseCase;
import com.example.account.modules.tiers.dto.AddThirdPartyContactRequest;
import com.example.account.modules.tiers.dto.ThirdPartyContactResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/tiers/third-parties/{thirdPartyId}/contacts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Third-Party Contacts", description = "Address book of a client or fournisseur (both are Kernel third parties) — used to pick who to notify when sharing a document")
public class ThirdPartyContactController {

    private final ThirdPartyContactUseCase thirdPartyContactUseCase;

    @GetMapping
    @Operation(operationId = "getThirdPartyContacts", summary = "List a third party's contacts (name + email) for the send-document contact picker")
    public Flux<ThirdPartyContactResponse> getThirdPartyContacts(@PathVariable UUID thirdPartyId) {
        log.info("GET /api/tiers/third-parties/{}/contacts", thirdPartyId);
        return thirdPartyContactUseCase.getContacts(thirdPartyId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "addThirdPartyContact", summary = "Add a new contact (name + email) for a third party")
    public Mono<ThirdPartyContactResponse> addContact(@PathVariable UUID thirdPartyId,
                                                        @RequestBody AddThirdPartyContactRequest request) {
        log.info("POST /api/tiers/third-parties/{}/contacts", thirdPartyId);
        return thirdPartyContactUseCase.addContact(thirdPartyId, request);
    }
}
