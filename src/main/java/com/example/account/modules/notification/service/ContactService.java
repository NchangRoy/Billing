package com.example.account.modules.notification.service;

import com.example.account.modules.notification.model.entity.Contact;
import com.example.account.modules.notification.model.enums.ContactStatus;
import com.example.account.modules.notification.repository.ContactRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactEmailService contactEmailService;

    @Value("${telegram.bot.username:KSM_BILL_bot}")
    private String botUsername;

    public ContactService(ContactRepository contactRepository, ContactEmailService contactEmailService) {
        this.contactRepository = contactRepository;
        this.contactEmailService = contactEmailService;
    }

    public Mono<Contact> addContact(UUID organizationId, String name, String email) {
        Instant now = Instant.now();
        Contact contact = Contact.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .organizationId(organizationId)
                .linkToken(UUID.randomUUID().toString().replace("-", ""))
                .status(ContactStatus.PENDING.name())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return contactRepository.save(contact)
                .flatMap(saved -> sendTelegramInvite(saved).thenReturn(saved));
    }

    /** No-op if already linked (has a chatId) — safe to call every time a TELEGRAM-channel live-notification activates. */
    public Mono<Void> inviteToTelegramIfUnlinked(Contact contact) {
        if (contact.getChatId() != null && !contact.getChatId().isBlank()) {
            return Mono.empty();
        }
        return sendTelegramInvite(contact);
    }

    private Mono<Void> sendTelegramInvite(Contact contact) {
        String deepLink = "https://t.me/" + botUsername + "?start=" + contact.getLinkToken();
        com.example.account.modules.notification.dto.SendContactInvitationEmailRequest request = new com.example.account.modules.notification.dto.SendContactInvitationEmailRequest();
        request.setOrganizationId(contact.getOrganizationId());
        request.setEmail(contact.getEmail());
        request.setName(contact.getName());
        request.setTelegramDeepLink(deepLink);
        // Best-effort: both callers (addContact, inviteToTelegramIfUnlinked) need the
        // contact itself to persist/activate even if Kernel's notification-core is
        // down or the caller's token has expired — losing the invite email is
        // recoverable (the admin can be told to resend), silently losing the contact
        // row they just added is not.
        return contactEmailService.sendInvitation(request)
                .onErrorResume(e -> {
                    log.error("Failed to send Telegram invite email to {}: {}", contact.getEmail(), e.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<Contact> linkChat(String linkToken, String chatId) {
        return contactRepository.findByLinkToken(linkToken)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown invite token")))
                .flatMap(contact -> {
                    contact.setChatId(chatId);
                    contact.setStatus(ContactStatus.ACTIVE.name());
                    contact.setUpdatedAt(Instant.now());
                    return contactRepository.save(contact);
                });
    }

    public Mono<Void> blockContact(UUID contactId) {
        return setStatus(contactId, ContactStatus.BLOCKED_BY_ADMIN);
    }

    public Mono<Void> unblockContact(UUID contactId) {
        return setStatus(contactId, ContactStatus.ACTIVE);
    }

    public Mono<Void> removeContact(UUID contactId) {
        return contactRepository.findById(contactId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found: " + contactId)))
                .flatMap(contactRepository::delete);
    }

    public Mono<Void> markBlockedByUser(String chatId) {
        return contactRepository.findByChatId(chatId)
                .flatMap(contact -> {
                    contact.setStatus(ContactStatus.BLOCKED_BY_USER.name());
                    contact.setUpdatedAt(Instant.now());
                    return contactRepository.save(contact);
                })
                .then();
    }

    public Flux<Contact> listContacts(UUID organizationId) {
        return contactRepository.findByOrganizationId(organizationId);
    }

    private Mono<Void> setStatus(UUID contactId, ContactStatus status) {
        return contactRepository.findById(contactId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found: " + contactId)))
                .flatMap(contact -> {
                    contact.setStatus(status.name());
                    contact.setUpdatedAt(Instant.now());
                    return contactRepository.save(contact);
                })
                .then();
    }
}
