package com.example.account.modules.notification.repository;

import com.example.account.modules.notification.model.entity.Contact;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ContactRepository extends ReactiveCrudRepository<Contact, UUID> {

    Mono<Contact> findByLinkToken(String linkToken);

    Flux<Contact> findByChatId(String chatId);

    Flux<Contact> findByOrganizationId(UUID organizationId);

    Mono<Contact> findByOrganizationIdAndEmail(UUID organizationId, String email);

    Flux<Contact> findByEmailAndChatIdIsNotNull(String email);
}
