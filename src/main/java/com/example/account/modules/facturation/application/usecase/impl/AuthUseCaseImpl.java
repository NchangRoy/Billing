package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.port.input.AuthUseCase;
import com.example.account.modules.facturation.domain.port.output.AuthServicePort;
import com.example.account.modules.facturation.dto.request.LoginRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUseCaseImpl implements AuthUseCase {

    private final AuthServicePort authServicePort;

    @Override
    public Mono<SellerAuthResponse> login(LoginRequest request) {
        return authServicePort.login(request.getUsername(), request.getPassword());
    }
}
