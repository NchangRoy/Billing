package com.example.account.modules.facturation.service;

import com.example.account.modules.facturation.adapter.output.persistence.FactureActorIndexEntity;
import com.example.account.modules.facturation.adapter.output.persistence.FactureActorIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * See FactureActorIndexEntity for why this exists — Billing-side index of
 * factureId -> client/fournisseur actor id for the two document types that
 * are actually remote-delegated to sales-core (Facture, FactureFournisseur).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FactureActorIndexService {

    private final FactureActorIndexRepository repository;

    /** Best-effort: a failure here shouldn't block document creation — the document just won't show in the portal until it's fixed. */
    public Mono<Void> record(UUID factureId, UUID actorId) {
        if (factureId == null || actorId == null) {
            return Mono.empty();
        }
        return repository.save(new FactureActorIndexEntity(factureId, actorId))
                .onErrorResume(e -> {
                    log.warn("Could not record actor index for facture {}: {}", factureId, e.getMessage());
                    return Mono.empty();
                })
                .then();
    }

    public Flux<UUID> findFactureIdsByActorId(UUID actorId) {
        return repository.findByActorId(actorId).map(FactureActorIndexEntity::getFactureId);
    }
}
