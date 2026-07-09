package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.port.input.SessionUseCase;
import com.example.account.modules.facturation.domain.port.output.SessionServicePort;
import com.example.account.modules.facturation.dto.request.CloseSessionRequest;
import com.example.account.modules.facturation.dto.request.CreateSessionRequest;
import com.example.account.modules.facturation.dto.request.UpdateSessionRequest;
import com.example.account.modules.facturation.dto.response.SessionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionUseCaseImpl implements SessionUseCase {

    private final SessionServicePort sessionServicePort;

    @Override
    public Mono<SessionResponse> open(CreateSessionRequest request) {
        return sessionServicePort.open(request);
    }

    @Override
    public Mono<SessionResponse> schedule(CreateSessionRequest request) {
        return sessionServicePort.schedule(request);
    }

    @Override
    public Mono<SessionResponse> start(UUID id) {
        return sessionServicePort.start(id);
    }

    @Override
    public Mono<SessionResponse> close(UUID id, CloseSessionRequest request) {
        return sessionServicePort.close(id, request);
    }

    @Override
    public Mono<SessionResponse> suspend(UUID id) {
        return sessionServicePort.suspend(id);
    }

    @Override
    public Mono<SessionResponse> resume(UUID id) {
        return sessionServicePort.resume(id);
    }

    @Override
    public Mono<SessionResponse> cancel(UUID id) {
        return sessionServicePort.cancel(id);
    }

    @Override
    public Mono<SessionResponse> reopen(UUID id) {
        return sessionServicePort.reopen(id);
    }

    @Override
    public Mono<SessionResponse> findById(UUID id) {
        return sessionServicePort.findById(id);
    }

    @Override
    public Flux<SessionResponse> findAll(UUID salesPointId, UUID sellerId, UUID organizationId, UUID agencyId) {
        return sessionServicePort.findAll(salesPointId, sellerId, organizationId, agencyId);
    }

    @Override
    public Mono<SessionResponse> update(UUID id, UpdateSessionRequest request) {
        return sessionServicePort.update(id, request);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return sessionServicePort.delete(id);
    }
}
