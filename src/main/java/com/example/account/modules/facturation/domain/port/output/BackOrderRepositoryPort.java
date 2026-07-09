package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.domain.model.BackOrder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BackOrderRepositoryPort {
    Mono<BackOrder> findById(UUID id);
    Flux<BackOrder> findAll();
    Flux<BackOrder> findByOrganizationId(UUID organizationId);
    Flux<BackOrder> findByAgencyId(UUID agencyId);
    Flux<BackOrder> findByIdBonLivraison(UUID idBonLivraison);
    Flux<BackOrder> findByIdClient(UUID idClient);
    Flux<BackOrder> findByCreatedBy(UUID createdBy);
    Mono<BackOrder> insert(BackOrder backOrder);
    Mono<BackOrder> save(BackOrder backOrder);
    Mono<Void> deleteById(UUID id);
    Mono<Void> delete(BackOrder backOrder);
}
