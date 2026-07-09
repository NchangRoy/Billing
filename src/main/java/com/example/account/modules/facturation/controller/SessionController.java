package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.domain.port.input.SessionUseCase;
import com.example.account.modules.facturation.dto.request.CloseSessionRequest;
import com.example.account.modules.facturation.dto.request.CreateSessionRequest;
import com.example.account.modules.facturation.dto.request.UpdateSessionRequest;
import com.example.account.modules.facturation.dto.response.SessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sessions", description = "A seller's occupancy of a sales point, from login to logout")
public class SessionController {

    private final SessionUseCase sessionUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Open a session")
    public Mono<SessionResponse> open(@Valid @RequestBody CreateSessionRequest request) {
        return sessionUseCase.open(request);
    }

    @PostMapping("/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Schedule a session for a seller to start themselves later")
    public Mono<SessionResponse> schedule(@Valid @RequestBody CreateSessionRequest request) {
        return sessionUseCase.schedule(request);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start a pending session (seller-initiated)")
    public Mono<SessionResponse> start(@PathVariable UUID id) {
        return sessionUseCase.start(id);
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close a session")
    public Mono<SessionResponse> close(@PathVariable UUID id, @Valid @RequestBody CloseSessionRequest request) {
        return sessionUseCase.close(id, request);
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend an open session")
    public Mono<SessionResponse> suspend(@PathVariable UUID id) {
        return sessionUseCase.suspend(id);
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "Resume a suspended session back to open")
    public Mono<SessionResponse> resume(@PathVariable UUID id) {
        return sessionUseCase.resume(id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending or open session")
    public Mono<SessionResponse> cancel(@PathVariable UUID id) {
        return sessionUseCase.cancel(id);
    }

    @PostMapping("/{id}/reopen")
    @Operation(summary = "Reopen a closed session")
    public Mono<SessionResponse> reopen(@PathVariable UUID id) {
        return sessionUseCase.reopen(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a session by ID")
    public Mono<SessionResponse> getById(@PathVariable UUID id) {
        return sessionUseCase.findById(id);
    }

    @GetMapping
    @Operation(summary = "List sessions, optionally filtered")
    public Flux<SessionResponse> getAll(
            @RequestParam(required = false) UUID salesPointId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) UUID agencyId) {
        return sessionUseCase.findAll(salesPointId, sellerId, organizationId, agencyId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a session")
    public Mono<SessionResponse> update(@PathVariable UUID id, @RequestBody UpdateSessionRequest request) {
        return sessionUseCase.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a session")
    public Mono<Void> delete(@PathVariable UUID id) {
        return sessionUseCase.delete(id);
    }
}
