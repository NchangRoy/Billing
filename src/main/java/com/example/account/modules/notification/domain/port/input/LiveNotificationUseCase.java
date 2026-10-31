package com.example.account.modules.notification.domain.port.input;

import com.example.account.modules.notification.dto.CreateLiveNotificationRequest;
import com.example.account.modules.notification.dto.LiveNotificationResponse;
import com.example.account.modules.notification.model.entity.PendingInvoiceSummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LiveNotificationUseCase {
    Mono<LiveNotificationResponse> create(CreateLiveNotificationRequest request);

    /** All notification rules (one per channel, typically) for the caller's own organization. */
    Flux<LiveNotificationResponse> listForOrganization();

    Mono<LiveNotificationResponse> updateDelay(UUID id, long delayMinutes);
    Mono<LiveNotificationResponse> setActive(UUID id, boolean active);

    /** Queues an invoice snapshot on every active live-notification rule for the organization. Called from facturation on invoice creation. */
    Mono<Void> enqueueInvoice(UUID organizationId, PendingInvoiceSummary invoice);
}
