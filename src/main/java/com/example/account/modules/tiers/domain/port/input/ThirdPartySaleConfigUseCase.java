package com.example.account.modules.tiers.domain.port.input;

import com.example.account.modules.tiers.dto.SaleConfigResponse;
import com.example.account.modules.tiers.dto.SetSaleConfigRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ThirdPartySaleConfigUseCase {
    Mono<SaleConfigResponse> setConfig(UUID thirdPartyId, SetSaleConfigRequest request);
    Mono<SaleConfigResponse> getConfig(UUID thirdPartyId);
}
