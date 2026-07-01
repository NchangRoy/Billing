package com.example.account.modules.tiers.controller;

import com.example.account.modules.tiers.domain.port.input.FournisseurUseCase;
import com.example.account.modules.tiers.dto.FournisseurResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/tiers/fournisseurs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fournisseurs", description = "Gestion des fournisseurs (tiers SUPPLIER depuis le Kernel)")
public class FournisseurController {

    private final FournisseurUseCase fournisseurUseCase;

    @GetMapping
    @Operation(summary = "Lister tous les fournisseurs actifs")
    public Flux<FournisseurResponse> getAllFournisseurs() {
        log.info("GET /api/tiers/fournisseurs");
        return fournisseurUseCase.getActiveFournisseurs();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un fournisseur par ID")
    public Mono<ResponseEntity<FournisseurResponse>> getFournisseurById(@PathVariable UUID id) {
        log.info("GET /api/tiers/fournisseurs/{}", id);
        return fournisseurUseCase.getFournisseurById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    @Operation(summary = "Nombre de fournisseurs actifs")
    public Mono<ResponseEntity<Long>> countFournisseurs() {
        return fournisseurUseCase.countActiveFournisseurs().map(ResponseEntity::ok);
    }
}
