package com.example.account.modules.notification.application.usecase.impl;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.notification.domain.port.input.LiveNotificationUseCase;
import com.example.account.modules.notification.domain.port.output.LiveNotificationServicePort;
import com.example.account.modules.notification.dto.CreateLiveNotificationRequest;
import com.example.account.modules.notification.dto.LiveNotificationResponse;
import com.example.account.modules.notification.model.entity.PendingInvoiceSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveNotificationUseCaseImpl implements LiveNotificationUseCase {

    private final LiveNotificationServicePort liveNotificationServicePort;

    @Override
    public Mono<LiveNotificationResponse> create(CreateLiveNotificationRequest request) {
        return ReactiveOrganizationContext.getOrganizationId()
                .flatMap(orgId -> {
                    request.setOrganizationId(orgId);
                    return liveNotificationServicePort.create(request);
                });
    }

    @Override
    public Flux<LiveNotificationResponse> listForOrganization() {
        return ReactiveOrganizationContext.getOrganizationId()
                .flatMapMany(liveNotificationServicePort::listByOrganization);
    }

    @Override
    public Mono<LiveNotificationResponse> updateDelay(UUID id, long delayMinutes) {
        return liveNotificationServicePort.updateDelay(id, delayMinutes);
    }

    @Override
    public Mono<LiveNotificationResponse> setActive(UUID id, boolean active) {
        return liveNotificationServicePort.setActive(id, active);
    }

    @Override
    public Mono<Void> enqueueInvoice(UUID organizationId, PendingInvoiceSummary invoice) {
        return liveNotificationServicePort.enqueueInvoice(organizationId, invoice);
    }
}
