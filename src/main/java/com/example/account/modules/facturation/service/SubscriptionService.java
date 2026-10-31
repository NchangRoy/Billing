package com.example.account.modules.facturation.service;

import com.example.account.modules.facturation.adapter.output.persistence.SubscriptionPersistenceEntity;
import com.example.account.modules.facturation.adapter.output.persistence.SubscriptionR2dbcRepository;
import com.example.account.modules.facturation.model.enums.SubscriptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One subscription per organization — created the first time a Try-Out
 * account with no existing seller picks a plan (see AuthUseCaseImpl).
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final int FREE_TRIAL_DURATION_DAYS = 30;

    private final SubscriptionR2dbcRepository repository;
    private final R2dbcEntityTemplate entityTemplate;

    public Mono<Boolean> exists(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).hasElement();
    }

    public Mono<SubscriptionPersistenceEntity> create(UUID organizationId, String planType) {
        SubscriptionType type = SubscriptionType.valueOf(planType);
        LocalDateTime now = LocalDateTime.now();
        SubscriptionPersistenceEntity entity = SubscriptionPersistenceEntity.builder()
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .type(type)
                .startedAt(now)
                .durationDays(FREE_TRIAL_DURATION_DAYS)
                .createdAt(now)
                .updatedAt(now)
                .build();
        // Client-generated id means Spring Data would otherwise guess this row
        // already exists and attempt an UPDATE — insert explicitly.
        return entityTemplate.insert(entity);
    }
}
