package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.domain.model.Taxes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface TaxeRepositoryPort {
    Mono<Taxes> findById(UUID id);
    Mono<Taxes> findByNomTaxe(String nomTaxe);
    Flux<Taxes> findAll();
    Flux<Taxes> findAllActiveTaxes();
    Flux<Taxes> findByTypeTaxe(String typeTaxe);
    Flux<Taxes> findActiveByTypeTaxe(String typeTaxe);
    Flux<Taxes> findByPorteTaxe(String porteTaxe);
    Flux<Taxes> findByPositionFiscale(String positionFiscale);
    Flux<Taxes> findByCalculTaxeBetween(BigDecimal min, BigDecimal max);
    Flux<Taxes> findByMontantBetween(BigDecimal min, BigDecimal max);
    Mono<Boolean> existsByNomTaxe(String nomTaxe);
    Mono<Boolean> existsById(UUID id);
    Mono<Taxes> save(Taxes taxe);
    Mono<Void> deleteById(UUID id);
    Mono<Long> countActiveTaxes();
    Mono<Long> countByTypeTaxe(String typeTaxe);
}
