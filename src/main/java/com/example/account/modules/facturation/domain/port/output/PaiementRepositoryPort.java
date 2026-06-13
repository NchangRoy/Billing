package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.domain.model.Paiement;
import com.example.account.modules.facturation.model.enums.TypePaiement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface PaiementRepositoryPort {
    Mono<Paiement> findById(UUID id);
    Flux<Paiement> findAll();
    Flux<Paiement> findByIdClient(UUID idClient);
    Flux<Paiement> findByIdFacture(UUID idFacture);
    Flux<Paiement> findByModePaiement(TypePaiement mode);
    Flux<Paiement> findByDateBetween(LocalDate start, LocalDate end);
    Mono<BigDecimal> sumMontantByClient(UUID idClient);
    Mono<BigDecimal> sumMontantByFacture(UUID idFacture);
    Mono<BigDecimal> sumMontantByDateBetween(LocalDate start, LocalDate end);
    Mono<Long> countByIdClient(UUID idClient);
    Mono<Long> countByModePaiement(TypePaiement mode);
    Mono<Paiement> save(Paiement paiement);
    Mono<Void> deleteById(UUID id);
    Mono<Boolean> existsById(UUID id);
}
