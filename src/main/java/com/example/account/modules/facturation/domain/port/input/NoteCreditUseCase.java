package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.NoteCreditRequest;
import com.example.account.modules.facturation.dto.response.NoteCreditResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface NoteCreditUseCase {
    Mono<NoteCreditResponse> createNoteCredit(NoteCreditRequest request);
    Mono<NoteCreditResponse> updateNoteCredit(UUID id, NoteCreditRequest request);
    Mono<NoteCreditResponse> getNoteCreditById(UUID id);
    Flux<NoteCreditResponse> getAllNoteCredits();
    Mono<Void> deleteNoteCredit(UUID id);
    Flux<NoteCreditResponse> getNotesCreditByOrganizationId(UUID organizationId);
    Flux<NoteCreditResponse> getNotesCreditByAgencyId(UUID agencyId);
    Flux<NoteCreditResponse> getNotesCreditBySellerId(UUID sellerId);
}
