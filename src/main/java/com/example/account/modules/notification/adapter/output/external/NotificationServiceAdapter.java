package com.example.account.modules.notification.adapter.output.external;

import com.example.account.modules.notification.domain.port.output.ContactServicePort;
import com.example.account.modules.notification.domain.port.output.LiveNotificationServicePort;
import com.example.account.modules.notification.dto.AddContactRequest;
import com.example.account.modules.notification.dto.ContactResponse;
import com.example.account.modules.notification.dto.CreateLiveNotificationRequest;
import com.example.account.modules.notification.dto.LiveNotificationResponse;
import com.example.account.modules.notification.model.entity.Contact;
import com.example.account.modules.notification.model.entity.LiveNotification;
import com.example.account.modules.notification.model.entity.PendingInvoiceSummary;
import com.example.account.modules.notification.service.ContactService;
import com.example.account.modules.notification.service.LiveNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Implements ContactServicePort and LiveNotificationServicePort locally.
 * Delegates all notification (Telegram and batch rule) operations to ContactService and LiveNotificationService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceAdapter implements ContactServicePort, LiveNotificationServicePort {

    private final ContactService contactService;
    private final LiveNotificationService liveNotificationService;

    @Override
    public Mono<ContactResponse> addContact(AddContactRequest request) {
        return contactService.addContact(request.getOrganizationId(), request.getName(), request.getEmail())
                .map(this::mapToResponse);
    }

    @Override
    public Flux<ContactResponse> listContacts(UUID organizationId) {
        return contactService.listContacts(organizationId)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<Void> blockContact(UUID contactId) {
        return contactService.blockContact(contactId);
    }

    @Override
    public Mono<Void> removeContact(UUID contactId) {
        return contactService.removeContact(contactId);
    }

    @Override
    public Mono<LiveNotificationResponse> create(CreateLiveNotificationRequest request) {
        boolean active = request.getActive() == null || request.getActive();
        return liveNotificationService.create(request.getOrganizationId(), request.getContactIds(), request.getDelayMinutes(),
                        request.getChannel(), active)
                .map(this::mapToResponse);
    }

    @Override
    public Flux<LiveNotificationResponse> listByOrganization(UUID organizationId) {
        return liveNotificationService.findByOrganizationId(organizationId)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<LiveNotificationResponse> updateDelay(UUID id, long delayMinutes) {
        return liveNotificationService.updateDelay(id, delayMinutes)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<LiveNotificationResponse> setActive(UUID id, boolean active) {
        return liveNotificationService.setActive(id, active)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<Void> enqueueInvoice(UUID organizationId, PendingInvoiceSummary invoice) {
        return liveNotificationService.enqueueInvoice(organizationId, invoice);
    }

    private ContactResponse mapToResponse(Contact contact) {
        if (contact == null) return null;
        ContactResponse res = new ContactResponse();
        res.setId(contact.getId());
        res.setName(contact.getName());
        res.setEmail(contact.getEmail());
        res.setOrganizationId(contact.getOrganizationId());
        res.setStatus(contact.getStatus());
        res.setLinked(contact.getChatId() != null && !contact.getChatId().isBlank());
        res.setCreatedAt(contact.getCreatedAt());
        return res;
    }

    private LiveNotificationResponse mapToResponse(LiveNotification ln) {
        if (ln == null) return null;
        LiveNotificationResponse res = new LiveNotificationResponse();
        res.setId(ln.getId());
        res.setOrganizationId(ln.getOrganizationId());
        res.setContactIds(ln.getContactIds() == null ? List.of() : Arrays.asList(ln.getContactIds()));
        res.setPendingFactureIds(ln.getPendingInvoices() == null ? List.of()
                : ln.getPendingInvoices().stream().map(PendingInvoiceSummary::getId).toList());
        res.setDelayMinutes(ln.getDelayMinutes());
        res.setLastSentAt(ln.getLastSentAt());
        res.setActive(ln.isActive());
        res.setChannel(ln.getChannel());
        return res;
    }
}
