package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.adapter.output.persistence.SellerPersistenceEntity;
import com.example.account.modules.facturation.domain.port.input.SessionUseCase;
import com.example.account.modules.facturation.domain.port.output.SessionServicePort;
import com.example.account.modules.facturation.dto.request.CloseSessionRequest;
import com.example.account.modules.facturation.dto.request.CreateSessionRequest;
import com.example.account.modules.facturation.dto.request.UpdateSessionRequest;
import com.example.account.modules.facturation.dto.response.SessionResponse;
import com.example.account.modules.notification.dto.SendSessionCreatedEmailRequest;
import com.example.account.modules.notification.dto.SendSessionUpdatedEmailRequest;
import com.example.account.modules.notification.service.SessionEmailService;
import com.example.account.modules.facturation.model.enums.SellerRole;
import com.example.account.modules.facturation.repository.SellerAgencyAssignmentRepository;
import com.example.account.modules.facturation.service.SellerLocalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionUseCaseImpl implements SessionUseCase {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SessionServicePort sessionServicePort;
    private final SellerLocalService sellerLocalService;
    private final SellerAgencyAssignmentRepository sellerAgencyAssignmentRepository;
    private final SessionEmailService sessionEmailService;

    @Override
    public Mono<SessionResponse> open(CreateSessionRequest request) {
        return sessionServicePort.open(request).flatMap(this::notifySessionCreated);
    }

    @Override
    public Mono<SessionResponse> schedule(CreateSessionRequest request) {
        return sessionServicePort.schedule(request).flatMap(this::notifySessionCreated);
    }

    /** Best-effort: a failed notification email must never fail session creation. */
    private Mono<SessionResponse> notifySessionCreated(SessionResponse session) {
        return sellerLocalService.findById(session.getSellerId())
                .flatMap(seller -> sessionEmailService.sendSessionCreated(toEmailRequest(seller, session)))
                .onErrorResume(e -> {
                    log.error("Failed to send session-created email for session {}: {}", session.getId(), e.getMessage());
                    return Mono.empty();
                })
                .thenReturn(session);
    }

    /** Best-effort: a failed notification email must never fail the status change itself. */
    private Mono<SessionResponse> notifySessionUpdated(SessionResponse session, String action) {
        return sellerLocalService.findById(session.getSellerId())
                .flatMap(seller -> sessionEmailService.sendSessionUpdated(toUpdateEmailRequest(seller, session, action)))
                .onErrorResume(e -> {
                    log.error("Failed to send session-updated email for session {}: {}", session.getId(), e.getMessage());
                    return Mono.empty();
                })
                .thenReturn(session);
    }

    private SendSessionUpdatedEmailRequest toUpdateEmailRequest(SellerPersistenceEntity seller, SessionResponse session, String action) {
        SendSessionUpdatedEmailRequest request = new SendSessionUpdatedEmailRequest();
        request.setOrganizationId(seller.getOrganizationId());
        request.setEmail(seller.getEmail());
        request.setFirstName(seller.getFirstName());
        request.setSessionType(session.getType() != null ? session.getType().name() : null);
        request.setAction(action);
        request.setStatus(session.getStatus() != null ? session.getStatus().name() : null);
        request.setOpeningAmount(session.getOpeningAmount());
        request.setClosingAmount(session.getClosingAmount());
        return request;
    }

    private SendSessionCreatedEmailRequest toEmailRequest(SellerPersistenceEntity seller, SessionResponse session) {
        SendSessionCreatedEmailRequest request = new SendSessionCreatedEmailRequest();
        request.setOrganizationId(seller.getOrganizationId());
        request.setEmail(seller.getEmail());
        request.setFirstName(seller.getFirstName());
        request.setSessionType(session.getType() != null ? session.getType().name() : null);
        request.setStatus(session.getStatus() != null ? session.getStatus().name() : null);
        request.setStartTime(session.getStartTime() != null ? session.getStartTime().format(DATE_FORMAT) : null);
        request.setOpeningAmount(session.getOpeningAmount());
        return request;
    }

    @Override
    public Mono<SessionResponse> start(UUID id) {
        return sessionServicePort.start(id).flatMap(session -> notifySessionUpdated(session, "démarrée"));
    }

    @Override
    public Mono<SessionResponse> close(UUID id, CloseSessionRequest request) {
        return sessionServicePort.close(id, request).flatMap(session -> notifySessionUpdated(session, "clôturée"));
    }

    @Override
    public Mono<SessionResponse> suspend(UUID id) {
        return sessionServicePort.suspend(id).flatMap(session -> notifySessionUpdated(session, "suspendue"));
    }

    @Override
    public Mono<SessionResponse> resume(UUID id) {
        return sessionServicePort.resume(id).flatMap(session -> notifySessionUpdated(session, "reprise"));
    }

    @Override
    public Mono<SessionResponse> cancel(UUID id) {
        return sessionServicePort.cancel(id).flatMap(session -> notifySessionUpdated(session, "annulée"));
    }

    @Override
    public Mono<SessionResponse> reopen(UUID id) {
        return sessionServicePort.reopen(id).flatMap(session -> notifySessionUpdated(session, "réouverte"));
    }

    @Override
    public Mono<SessionResponse> findById(UUID id) {
        return sessionServicePort.findById(id);
    }

    @Override
    public Flux<SessionResponse> findAll(UUID salesPointId, UUID sellerId, UUID organizationId, UUID agencyId) {
        return sessionServicePort.findAll(salesPointId, sellerId, organizationId, agencyId);
    }

    /**
     * Scopes the org's Sessions page to what the caller is actually allowed
     * to see: an owner sees every session in the org, an agency manager only
     * those under their own agency, anyone else only their own. callerSellerId
     * is caller-supplied (same trust model as the sellerId filter on findAll
     * above — there's no caller identity in the Kernel access token itself to
     * resolve this from, see AuthServiceAdapter), but the ROLE used to decide
     * the scope is always looked up fresh from our own sellers table, never
     * trusted from the request.
     */
    @Override
    public Flux<SessionResponse> findAllScopedForCaller(UUID organizationId, UUID callerSellerId) {
        return sellerLocalService.findById(callerSellerId)
                .filter(seller -> organizationId.equals(seller.getOrganizationId()))
                .flatMapMany(seller -> {
                    String role = seller.getRole();
                    if (SellerRole.OWNER.name().equals(role)) {
                        return sessionServicePort.findAll(null, null, organizationId, null);
                    }
                    if (SellerRole.AGENCY_MANAGER.name().equals(role)) {
                        return sellerAgencyAssignmentRepository.findBySellerId(callerSellerId)
                                .flatMapMany(own -> sessionServicePort.findAll(null, null, organizationId, own.getAgencyId()));
                    }
                    // SELLER / POS_SELLER: only their own sessions.
                    return sessionServicePort.findAll(null, callerSellerId, organizationId, null);
                });
    }

    @Override
    public Mono<SessionResponse> update(UUID id, UpdateSessionRequest request) {
        return sessionServicePort.update(id, request).flatMap(session -> notifySessionUpdated(session, "mise à jour"));
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return sessionServicePort.delete(id);
    }
}
