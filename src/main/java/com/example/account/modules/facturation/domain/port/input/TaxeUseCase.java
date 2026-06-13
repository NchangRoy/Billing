package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.TaxeCreateRequest;
import com.example.account.modules.facturation.dto.request.TaxeUpdateRequest;
import com.example.account.modules.facturation.dto.response.TaxeResponse;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface TaxeUseCase {
    Mono<TaxeResponse> createTaxe(TaxeCreateRequest request);
    Mono<TaxeResponse> updateTaxe(UUID taxeId, TaxeUpdateRequest request);
    Mono<TaxeResponse> getTaxeById(UUID taxeId);
    Mono<TaxeResponse> getTaxeByNom(String nomTaxe);
    Flux<TaxeResponse> getAllTaxes();
    Flux<TaxeResponse> getAllTaxes(Pageable pageable);
    Flux<TaxeResponse> getActiveTaxes();
    Flux<TaxeResponse> getTaxesByType(String typeTaxe);
    Flux<TaxeResponse> getActiveTaxesByType(String typeTaxe);
    Flux<TaxeResponse> getTaxesByPorte(String porteTaxe);
    Flux<TaxeResponse> getTaxesByPositionFiscale(String positionFiscale);
    Flux<TaxeResponse> getTaxesByCalculRange(BigDecimal minTaux, BigDecimal maxTaux);
    Flux<TaxeResponse> getTaxesByMontantRange(BigDecimal minMontant, BigDecimal maxMontant);
    Mono<Void> deleteTaxe(UUID taxeId);
    Mono<TaxeResponse> activerTaxe(UUID taxeId);
    Mono<TaxeResponse> desactiverTaxe(UUID taxeId);
    Mono<Long> countActiveTaxes();
    Mono<Long> countByType(String typeTaxe);
}
