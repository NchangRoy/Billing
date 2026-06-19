package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.BondeReceptionCreateRequest;
import com.example.account.modules.facturation.dto.response.BondeReceptionResponse;
import com.example.account.modules.facturation.service.BonReceptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facturation/bon-receptions")
@RequiredArgsConstructor
public class BondeReceptionController {

    private final BonReceptionService bonReceptionService;

    @GetMapping
    public Flux<BondeReceptionResponse> getBons() {
        return bonReceptionService.getAllBondeReception();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<BondeReceptionResponse>> getBonById(@PathVariable UUID id) {
        return bonReceptionService.getBondeReceptionById(id)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<BondeReceptionResponse>> createBon(@RequestBody BondeReceptionCreateRequest dto) {
        return bonReceptionService.createBondeReception(dto)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<BondeReceptionResponse>> updateBon(@PathVariable UUID id, @RequestBody BondeReceptionResponse updatedData) {
        return bonReceptionService.updateBondeReception(id, updatedData)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteBon(@PathVariable UUID id) {
        return bonReceptionService.deleteBondeReception(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/organisation/{organizationId}")
    public Flux<BondeReceptionResponse> getByOrganizationId(@PathVariable UUID organizationId) {
        return bonReceptionService.getByOrganizationId(organizationId);
    }

    @GetMapping("/agence/{agencyId}")
    public Flux<BondeReceptionResponse> getByAgencyId(@PathVariable UUID agencyId) {
        return bonReceptionService.getByAgencyId(agencyId);
    }
}
