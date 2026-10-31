package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.facturation.domain.port.output.SessionServicePort;
import com.example.account.modules.facturation.dto.enums.SessionStatus;
import com.example.account.modules.facturation.dto.enums.SessionType;
import com.example.account.modules.facturation.dto.request.CloseSessionRequest;
import com.example.account.modules.facturation.dto.request.CreateSessionRequest;
import com.example.account.modules.facturation.dto.request.UpdateSessionRequest;
import com.example.account.modules.facturation.dto.response.SessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Local, database-backed replacement for the old sales-core-proxying
 * SessionServiceAdapter. Sessions only ever depended on sales-core to
 * validate sellerId against its own seller roster — once sellers moved to
 * being purely local (see SellerPersistenceEntity), that roster stopped
 * knowing about sellers created through Billing's own invite/add-from-employee
 * flow, and sales-core started rejecting every session open for them with
 * "Seller not found". Sessions never needed anything else from sales-core, so
 * this drops the dependency entirely rather than re-syncing sellers into a
 * global roster — the exact problem the local-sellers migration got away from.
 */
@Service
@RequiredArgsConstructor
public class SessionLocalServiceAdapter implements SessionServicePort {

    private final SessionR2dbcRepository repository;
    private final R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<SessionResponse> open(CreateSessionRequest request) {
        return create(request, SessionStatus.OPEN);
    }

    @Override
    public Mono<SessionResponse> schedule(CreateSessionRequest request) {
        return create(request, SessionStatus.PENDING);
    }

    /**
     * organizationId is resolved from the caller's own context rather than
     * request.getOrganizationId() — a POS-type request never sends it (the
     * frontend only sends organizationId for SALES sessions, agencyId/
     * salesPointId for POS), but every session still needs one so it shows up
     * when the org's Sessions page filters findAll() by organizationId.
     */
    private Mono<SessionResponse> create(CreateSessionRequest request, SessionStatus status) {
        LocalDateTime requestedStart = request.getStartTime();
        LocalDateTime requestedEnd = request.getEndTime();
        if (requestedStart != null && requestedEnd != null && !requestedEnd.isAfter(requestedStart)) {
            // Seen live from the frontend's native datetime-local picker (an
            // easy AM/PM mis-click): an endTime at or before startTime makes
            // the session look already-ended the instant it opens, with no
            // obvious reason why to the seller.
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must be after startTime"));
        }
        return ReactiveOrganizationContext.getOrganizationId()
                .flatMap(organizationId -> {
                    LocalDateTime now = LocalDateTime.now();
                    SessionPersistenceEntity entity = SessionPersistenceEntity.builder()
                            .id(UUID.randomUUID())
                            .type(request.getType().name())
                            .status(status.name())
                            .salesPointId(request.getSalesPointId())
                            .organizationId(organizationId)
                            .agencyId(request.getAgencyId())
                            .sellerId(request.getSellerId())
                            .openingAmount(request.getOpeningAmount())
                            .startTime(request.getStartTime() != null ? request.getStartTime() : now)
                            .endTime(request.getEndTime())
                            .locked(false)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    // Client-generated id means Spring Data would otherwise guess this
                    // row already exists and attempt an UPDATE — insert explicitly
                    // (same reasoning as SellerLocalService.create).
                    return entityTemplate.insert(entity);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<SessionResponse> start(UUID id) {
        return transition(id, SessionStatus.OPEN, entity -> entity.setLoginTime(LocalDateTime.now()));
    }

    @Override
    public Mono<SessionResponse> close(UUID id, CloseSessionRequest request) {
        return transition(id, SessionStatus.CLOSED, entity -> {
            entity.setClosingAmount(request.getClosingAmount());
            entity.setLogoutTime(LocalDateTime.now());
        });
    }

    @Override
    public Mono<SessionResponse> suspend(UUID id) {
        return transition(id, SessionStatus.SUSPENDED, entity -> { });
    }

    @Override
    public Mono<SessionResponse> resume(UUID id) {
        return transition(id, SessionStatus.OPEN, entity -> { });
    }

    @Override
    public Mono<SessionResponse> cancel(UUID id) {
        return transition(id, SessionStatus.CANCELLED, entity -> { });
    }

    @Override
    public Mono<SessionResponse> reopen(UUID id) {
        return transition(id, SessionStatus.REOPENED, entity -> {
            entity.setClosingAmount(null);
            entity.setLogoutTime(null);
        });
    }

    private Mono<SessionResponse> transition(UUID id, SessionStatus newStatus, Consumer<SessionPersistenceEntity> mutate) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found: " + id)))
                .flatMap(entity -> {
                    mutate.accept(entity);
                    entity.setStatus(newStatus.name());
                    entity.setUpdatedAt(LocalDateTime.now());
                    return repository.save(entity);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<SessionResponse> findById(UUID id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found: " + id)))
                .map(this::toResponse);
    }

    @Override
    public Flux<SessionResponse> findAll(UUID salesPointId, UUID sellerId, UUID organizationId, UUID agencyId) {
        Criteria criteria = Criteria.empty();
        if (salesPointId != null) criteria = criteria.and("sales_point_id").is(salesPointId);
        if (sellerId != null) criteria = criteria.and("seller_id").is(sellerId);
        if (organizationId != null) criteria = criteria.and("organization_id").is(organizationId);
        if (agencyId != null) criteria = criteria.and("agency_id").is(agencyId);
        return entityTemplate.select(Query.query(criteria), SessionPersistenceEntity.class)
                .map(this::toResponse);
    }

    @Override
    public Mono<SessionResponse> update(UUID id, UpdateSessionRequest request) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found: " + id)))
                .flatMap(entity -> {
                    if (request.getOpeningAmount() != null) entity.setOpeningAmount(request.getOpeningAmount());
                    if (request.getLocked() != null) entity.setLocked(request.getLocked());
                    entity.setUpdatedAt(LocalDateTime.now());
                    return repository.save(entity);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private SessionResponse toResponse(SessionPersistenceEntity entity) {
        SessionResponse response = new SessionResponse();
        response.setId(entity.getId());
        response.setSalesPointId(entity.getSalesPointId());
        response.setOrganizationId(entity.getOrganizationId());
        response.setAgencyId(entity.getAgencyId());
        response.setSellerId(entity.getSellerId());
        response.setType(entity.getType() != null ? SessionType.valueOf(entity.getType()) : null);
        response.setStatus(entity.getStatus() != null ? SessionStatus.valueOf(entity.getStatus()) : null);
        response.setOpeningAmount(entity.getOpeningAmount());
        response.setClosingAmount(entity.getClosingAmount());
        response.setStartTime(entity.getStartTime());
        response.setEndTime(entity.getEndTime());
        response.setLoginTime(entity.getLoginTime());
        response.setLogoutTime(entity.getLogoutTime());
        response.setLocked(entity.isLocked());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
