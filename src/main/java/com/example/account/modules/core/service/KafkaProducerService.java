package com.example.account.modules.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * KafkaProducer.send() blocks the calling thread synchronously (up to
     * max.block.ms, default 60s) while it waits for cluster metadata — it
     * doesn't return a Future until that's resolved. Every caller here fires
     * this fire-and-forget (.subscribe() with no awaiting), straight from
     * whatever thread is running the main request flow (a WebFlux/Netty
     * event-loop thread) — without subscribeOn, that 60s block happens
     * ON that thread, stalling the HTTP response for the actual business
     * operation (e.g. "accept quotation") that already completed, any time
     * the broker is unreachable. subscribeOn(boundedElastic) moves the
     * blocking call off the event loop so a dead Kafka broker degrades to
     * "this one background event never got published" instead of every
     * caller silently hanging for a minute.
     */
    public Mono<SendResult<String, Object>> sendMessage(String topic, String key, Object message) {
        log.info("Envoi d'un message au topic [{}] avec clé [{}]", topic, key);

        return Mono.fromFuture(() -> kafkaTemplate.send(topic, key, message))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> log.info("Message envoyé avec succès au topic [{}] avec clé [{}]: offset={}, partition={}",
                        topic, key, result.getRecordMetadata().offset(), result.getRecordMetadata().partition()))
                .doOnError(ex -> log.error("Échec de l'envoi du message au topic [{}] avec clé [{}]: {}",
                        topic, key, ex.getMessage()))
                .onErrorResume(ex -> Mono.empty());
    }

    public Mono<SendResult<String, Object>> sendMessage(String topic, Object message) {
        return sendMessage(topic, null, message);
    }
}
