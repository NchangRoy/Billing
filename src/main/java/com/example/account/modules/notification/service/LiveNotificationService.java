package com.example.account.modules.notification.service;

import com.example.account.modules.notification.domain.port.output.NotificationPort;
import com.example.account.modules.notification.model.entity.Contact;
import com.example.account.modules.notification.model.entity.LiveNotification;
import com.example.account.modules.notification.model.entity.PendingInvoiceSummary;
import com.example.account.modules.notification.model.enums.ContactStatus;
import com.example.account.modules.notification.model.enums.NotificationChannel;
import com.example.account.modules.notification.repository.ContactRepository;
import com.example.account.modules.notification.repository.LiveNotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class LiveNotificationService {

    private final LiveNotificationRepository liveNotificationRepository;
    private final ContactRepository contactRepository;
    private final ContactService contactService;
    private final NotificationPort telegramNotificationPort;
    private final NotificationPort emailNotificationPort;

    public LiveNotificationService(LiveNotificationRepository liveNotificationRepository,
                                    ContactRepository contactRepository,
                                    ContactService contactService,
                                    @Qualifier("telegramNotificationPort") NotificationPort telegramNotificationPort,
                                    @Qualifier("emailNotificationPort") NotificationPort emailNotificationPort) {
        this.liveNotificationRepository = liveNotificationRepository;
        this.contactRepository = contactRepository;
        this.contactService = contactService;
        this.telegramNotificationPort = telegramNotificationPort;
        this.emailNotificationPort = emailNotificationPort;
    }

    // ─── Rule management ────────────────────────────────────────────────────

    public Mono<LiveNotification> create(UUID organizationId, List<UUID> contactIds, long delayMinutes,
                                          NotificationChannel channel, boolean active) {
        Instant now = Instant.now();
        LiveNotification liveNotification = LiveNotification.builder()
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .contactIds(contactIds == null ? new UUID[0] : contactIds.toArray(new UUID[0]))
                .pendingInvoices(new ArrayList<>())
                .delayMinutes(delayMinutes)
                .channel(channel == null ? NotificationChannel.TELEGRAM : channel)
                .active(active)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return liveNotificationRepository.save(liveNotification)
                .flatMap(saved -> inviteTelegramContactsIfNeeded(saved).thenReturn(saved));
    }

    public Flux<LiveNotification> findByOrganizationId(UUID organizationId) {
        return liveNotificationRepository.findByOrganizationId(organizationId);
    }

    public Mono<LiveNotification> addContact(UUID liveNotificationId, UUID contactId) {
        return findOrThrow(liveNotificationId).flatMap(ln -> {
            ln.setContactIds(withAdded(ln.getContactIds(), contactId));
            ln.setUpdatedAt(Instant.now());
            return liveNotificationRepository.save(ln);
        });
    }

    public Mono<LiveNotification> removeContact(UUID liveNotificationId, UUID contactId) {
        return findOrThrow(liveNotificationId).flatMap(ln -> {
            ln.setContactIds(withRemoved(ln.getContactIds(), contactId));
            ln.setUpdatedAt(Instant.now());
            return liveNotificationRepository.save(ln);
        });
    }

    public Mono<LiveNotification> updateDelay(UUID liveNotificationId, long delayMinutes) {
        return findOrThrow(liveNotificationId).flatMap(ln -> {
            ln.setDelayMinutes(delayMinutes);
            ln.setUpdatedAt(Instant.now());
            return liveNotificationRepository.save(ln);
        });
    }

    /** Turning a TELEGRAM rule on (re)sends the bot deep-link to any contact that still isn't linked. */
    public Mono<LiveNotification> setActive(UUID liveNotificationId, boolean active) {
        return findOrThrow(liveNotificationId).flatMap(ln -> {
            ln.setActive(active);
            ln.setUpdatedAt(Instant.now());
            return liveNotificationRepository.save(ln);
        }).flatMap(saved -> inviteTelegramContactsIfNeeded(saved).thenReturn(saved));
    }

    private Mono<Void> inviteTelegramContactsIfNeeded(LiveNotification ln) {
        if (!ln.isActive() || ln.getChannel() != NotificationChannel.TELEGRAM || ln.getContactIds() == null) {
            return Mono.empty();
        }
        return contactRepository.findAllById(java.util.Arrays.asList(ln.getContactIds()))
                .flatMap(contactService::inviteToTelegramIfUnlinked)
                .then();
    }

    // ─── Queueing ────────────────────────────────────────────────────────────

    /**
     * Queues an invoice snapshot on every active LiveNotification rule for the
     * organization, without ever touching the factures table itself. Called
     * from a real HTTP request (invoice creation), so a caller token exists
     * here — the whole point of snapshotting now rather than at flush time.
     */
    public Mono<Void> enqueueInvoice(UUID organizationId, PendingInvoiceSummary invoice) {
        return liveNotificationRepository.findByOrganizationId(organizationId)
                .filter(LiveNotification::isActive)
                .flatMap(ln -> {
                    ln.setPendingInvoices(withAdded(ln.getPendingInvoices(), invoice));
                    ln.setUpdatedAt(Instant.now());
                    return liveNotificationRepository.save(ln);
                })
                .then();
    }

    // ─── Flush ───────────────────────────────────────────────────────────────

    @Scheduled(fixedDelayString = "${live-notification.scheduler.tick-ms:60000}")
    public void flushDueScheduled() {
        flushDue().subscribe(
                null,
                ex -> log.warn("Live notification flush failed: {}", ex.getMessage())
        );
    }

    public Mono<Void> flushDue() {
        return liveNotificationRepository.findByActiveTrue()
                .filter(this::isDue)
                .flatMap(this::flushOne)
                .then();
    }

    private boolean isDue(LiveNotification ln) {
        if (ln.getPendingInvoices() == null || ln.getPendingInvoices().isEmpty()) {
            return false;
        }
        if (ln.getLastSentAt() == null) {
            return true;
        }
        Instant dueAt = ln.getLastSentAt().plus(ln.getDelayMinutes(), ChronoUnit.MINUTES);
        return !Instant.now().isBefore(dueAt);
    }

    /** No Kernel/sales-core call here at all — everything needed is already in the snapshot. */
    private Mono<Void> flushOne(LiveNotification ln) {
        List<UUID> contactIds = java.util.Arrays.asList(ln.getContactIds());
        boolean isTelegram = ln.getChannel() == NotificationChannel.TELEGRAM;

        return contactRepository.findAllById(contactIds)
                .filter(c -> isTelegram ? isTelegramReady(c) : hasEmail(c))
                .collectList()
                .flatMap(recipients -> {
                    if (recipients.isEmpty()) {
                        return clearQueue(ln);
                    }
                    NotificationPort port = isTelegram ? telegramNotificationPort : emailNotificationPort;
                    return port.send(buildMessage(ln.getPendingInvoices()), recipients)
                            .then(clearQueue(ln));
                });
    }

    private boolean isTelegramReady(Contact contact) {
        return ContactStatus.ACTIVE.name().equals(contact.getStatus())
                && contact.getChatId() != null && !contact.getChatId().isBlank();
    }

    private boolean hasEmail(Contact contact) {
        return contact.getEmail() != null && !contact.getEmail().isBlank();
    }

    private Mono<Void> clearQueue(LiveNotification ln) {
        ln.setPendingInvoices(new ArrayList<>());
        ln.setLastSentAt(Instant.now());
        ln.setUpdatedAt(Instant.now());
        return liveNotificationRepository.save(ln).then();
    }

    private String buildMessage(List<PendingInvoiceSummary> invoices) {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 ").append(invoices.size())
                .append(invoices.size() == 1 ? " new invoice" : " new invoices").append("\n");
        for (PendingInvoiceSummary invoice : invoices) {
            sb.append("• ")
                    .append(invoice.getNumeroFacture() != null ? invoice.getNumeroFacture() : invoice.getId())
                    .append(" — ")
                    .append(invoice.getMontant())
                    .append(invoice.getDevise() != null ? " " + invoice.getDevise() : "")
                    .append(invoice.getNomClient() != null ? " — " + invoice.getNomClient() : "")
                    .append("\n");
        }
        return sb.toString().trim();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private UUID[] withAdded(UUID[] current, UUID id) {
        java.util.Set<UUID> set = current == null ? new java.util.LinkedHashSet<>() : new java.util.LinkedHashSet<>(java.util.Arrays.asList(current));
        set.add(id);
        return set.toArray(new UUID[0]);
    }

    private UUID[] withRemoved(UUID[] current, UUID id) {
        if (current == null) {
            return new UUID[0];
        }
        return java.util.Arrays.stream(current).filter(existing -> !existing.equals(id)).toArray(UUID[]::new);
    }

    /** Dedup by invoice id, keeping the first occurrence's position. */
    private List<PendingInvoiceSummary> withAdded(List<PendingInvoiceSummary> current, PendingInvoiceSummary invoice) {
        Map<UUID, PendingInvoiceSummary> byId = new LinkedHashMap<>();
        if (current != null) {
            for (PendingInvoiceSummary existing : current) {
                byId.put(existing.getId(), existing);
            }
        }
        byId.put(invoice.getId(), invoice);
        return new ArrayList<>(byId.values());
    }

    private Mono<LiveNotification> findOrThrow(UUID id) {
        return liveNotificationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Live notification not found: " + id)));
    }
}
