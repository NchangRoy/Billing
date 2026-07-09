package com.example.account.modules.tiers.application.usecase.impl;

import com.example.account.modules.tiers.domain.port.input.ThirdPartySaleConfigUseCase;
import com.example.account.modules.tiers.domain.port.output.ThirdPartySaleConfigServicePort;
import com.example.account.modules.tiers.dto.SaleConfigResponse;
import com.example.account.modules.tiers.dto.SetSaleConfigRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThirdPartySaleConfigUseCaseImpl implements ThirdPartySaleConfigUseCase {

    private final ThirdPartySaleConfigServicePort thirdPartySaleConfigServicePort;

    @Override
    public Mono<SaleConfigResponse> setConfig(UUID thirdPartyId, SetSaleConfigRequest request) {
        return thirdPartySaleConfigServicePort.setConfig(thirdPartyId, request);
    }

    @Override
    public Mono<SaleConfigResponse> getConfig(UUID thirdPartyId) {
        return thirdPartySaleConfigServicePort.getConfig(thirdPartyId);
    }
}
