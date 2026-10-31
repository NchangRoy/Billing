package com.example.account.modules.notification.domain.port.output;

import com.example.account.modules.notification.dto.CreateLiveNotificationRequest;
import com.example.account.modules.notification.dto.LiveNotificationResponse;
import com.example.account.modules.notification.model.entity.PendingInvoiceSummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LiveNotificationServicePort {
    Mono<LiveNotificationResponse> create(CreateLiveNotificationRequest request);
    Flux<LiveNotificationResponse> listByOrganization(UUID organizationId);
    Mono<LiveNotificationResponse> updateDelay(UUID id, long delayMinutes);
    Mono<LiveNotificationResponse> setActive(UUID id, boolean active);
    Mono<Void> enqueueInvoice(UUID organizationId, PendingInvoiceSummary invoice);
}
