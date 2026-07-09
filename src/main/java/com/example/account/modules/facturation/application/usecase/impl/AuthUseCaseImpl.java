package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.port.input.AuthUseCase;
import com.example.account.modules.facturation.domain.port.output.AuthServicePort;
import com.example.account.modules.facturation.dto.request.ChangePasswordRequest;
import com.example.account.modules.facturation.dto.request.LoginRequest;
import com.example.account.modules.facturation.dto.request.PinLoginRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import com.example.account.modules.settings.service.SettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUseCaseImpl implements AuthUseCase {

    private final AuthServicePort authServicePort;
    private final SettingService settingService;

    @Override
    public Mono<SellerAuthResponse> login(LoginRequest request) {
        return authServicePort.login(request.getUsername(), request.getPassword())
                .flatMap(this::withOrganizationSettings);
    }

    @Override
    public Mono<SellerAuthResponse> loginByPin(PinLoginRequest request) {
        return authServicePort.loginByPin(request)
                .flatMap(this::withOrganizationSettings);
    }

    @Override
    public Mono<SellerAuthResponse> changePassword(ChangePasswordRequest request) {
        return authServicePort.changePassword(request)
                .flatMap(this::withOrganizationSettings);
    }

    @Override
    public Mono<SellerAuthResponse> tryOut(LoginRequest request) {
        return authServicePort.tryOut(request.getUsername(), request.getPassword(), request.getOrganizationId())
                .flatMap(this::withOrganizationSettings);
    }

    private Mono<SellerAuthResponse> withOrganizationSettings(SellerAuthResponse response) {
        if (response.getOrganizationId() == null) {
            return Mono.just(response);
        }
        return Mono.zip(
                        settingService.getOrganizationSettings(response.getOrganizationId()),
                        settingService.listSequenceSettings(response.getOrganizationId()).collectList()
                )
                .map(tuple -> {
                    response.setOrganizationSettings(tuple.getT1());
                    response.setDocumentNumberingSettings(tuple.getT2());
                    // organizationLogoUri is a snapshot taken at seller-creation
                    // time and never updated — every document preview reads
                    // this field, so keep it in sync with whatever's actually
                    // configured on the Settings page (organizationSettings.uri
                    // is fetched fresh above, on every login).
                    String configuredLogo = tuple.getT1().getUri();
                    if (configuredLogo != null && !configuredLogo.isBlank()) {
                        response.setOrganizationLogoUri(configuredLogo);
                    }
                    return response;
                })
                .onErrorReturn(response);
    }
}
