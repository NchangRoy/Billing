package com.example.account.modules.tiers.domain.port.output;

import com.example.account.modules.tiers.dto.AddThirdPartyContactRequest;
import com.example.account.modules.tiers.dto.ThirdPartyContactResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Lists/creates a client or fournisseur's contacts (name + email) so the
 * caller can pick who to notify when sharing a document — both resolve
 * through the same Kernel third-party record (GET /api/third-parties/{id}),
 * whose partyId is the actor id that common-core's address book is keyed by.
 */
public interface ThirdPartyContactServicePort {
    Flux<ThirdPartyContactResponse> getContacts(UUID thirdPartyId);

    Mono<ThirdPartyContactResponse> addContact(UUID thirdPartyId, AddThirdPartyContactRequest request);

    /** The Kernel actor id behind a client/fournisseur third-party record — same resolution getContacts/addContact use internally, exposed for callers (e.g. document creation) that just need the id. */
    Mono<UUID> resolveActorId(UUID thirdPartyId);
}
