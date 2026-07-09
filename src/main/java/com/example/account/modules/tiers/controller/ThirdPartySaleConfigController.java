package com.example.account.modules.tiers.controller;

import com.example.account.modules.tiers.domain.port.input.ThirdPartySaleConfigUseCase;
import com.example.account.modules.tiers.dto.SaleConfigResponse;
import com.example.account.modules.tiers.dto.SetSaleConfigRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/tiers/third-parties/{thirdPartyId}/sale-config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Third-Party Sale Config", description = "Allowed sale sizes and VAT applicability for a client or fournisseur (both are Kernel third parties)")
public class ThirdPartySaleConfigController {

    private final ThirdPartySaleConfigUseCase thirdPartySaleConfigUseCase;

    @PostMapping
    @Operation(summary = "Set the sale config for a third party")
    public Mono<SaleConfigResponse> set(@PathVariable UUID thirdPartyId, @RequestBody SetSaleConfigRequest request) {
        return thirdPartySaleConfigUseCase.setConfig(thirdPartyId, request);
    }

    @GetMapping
    @Operation(summary = "Get the sale config for a third party")
    public Mono<SaleConfigResponse> get(@PathVariable UUID thirdPartyId) {
        return thirdPartySaleConfigUseCase.getConfig(thirdPartyId);
    }
}
