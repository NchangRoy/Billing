package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.CloseSessionRequest;
import com.example.account.modules.facturation.dto.request.CreateSessionRequest;
import com.example.account.modules.facturation.dto.request.UpdateSessionRequest;
import com.example.account.modules.facturation.dto.response.SessionResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SessionUseCase {
    Mono<SessionResponse> open(CreateSessionRequest request);
    Mono<SessionResponse> schedule(CreateSessionRequest request);
    Mono<SessionResponse> start(UUID id);
    Mono<SessionResponse> close(UUID id, CloseSessionRequest request);
    Mono<SessionResponse> suspend(UUID id);
    Mono<SessionResponse> resume(UUID id);
    Mono<SessionResponse> cancel(UUID id);
    Mono<SessionResponse> reopen(UUID id);
    Mono<SessionResponse> findById(UUID id);
    Flux<SessionResponse> findAll(UUID salesPointId, UUID sellerId, UUID organizationId, UUID agencyId);
    Mono<SessionResponse> update(UUID id, UpdateSessionRequest request);
    Mono<Void> delete(UUID id);
}
