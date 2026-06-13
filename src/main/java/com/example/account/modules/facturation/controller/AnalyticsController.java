package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.domain.port.output.FactureRepositoryPort;
import com.example.account.modules.tiers.domain.port.output.ClientRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "API d'analyses et rapports (WebFlux)")
public class AnalyticsController {

    private final FactureRepositoryPort factureRepository;
    private final ClientRepositoryPort clientRepository;

    @GetMapping("/ventes/periode")
    @Operation(summary = "Rapport des ventes par période")
    public Mono<ResponseEntity<Map<String, Object>>> getRapportVentes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Rapport des ventes entre {} et {}", startDate, endDate);

        Mono<BigDecimal> sumMono = factureRepository.sumMontantByDateBetween(startDate, endDate)
                .defaultIfEmpty(BigDecimal.ZERO);
        Mono<Long> countMono = factureRepository.countByDateBetween(startDate, endDate)
                .defaultIfEmpty(0L);

        return Mono.zip(sumMono, countMono)
                .map(tuple -> {
                    BigDecimal montantTotal = tuple.getT1();
                    Long nombreFactures = tuple.getT2();

                    Map<String, Object> rapport = new HashMap<>();
                    rapport.put("periode", Map.of("debut", startDate, "fin", endDate));
                    rapport.put("montantTotal", montantTotal);
                    rapport.put("nombreFactures", nombreFactures);
                    rapport.put("montantMoyen", nombreFactures > 0
                            ? montantTotal.divide(BigDecimal.valueOf(nombreFactures), 2, java.math.RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);

                    return ResponseEntity.ok(rapport);
                });
    }

    @GetMapping("/clients/top")
    @Operation(summary = "Top clients par chiffre d'affaires")
    public Mono<ResponseEntity<List<Map<String, Object>>>> getTopClients(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Récupération du top {} clients", limit);

        // Implémentation simplifiée - à compléter avec une vraie requête
        List<Map<String, Object>> topClients = new ArrayList<>();

        return Mono.just(ResponseEntity.ok(topClients));
    }
}
