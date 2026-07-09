package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.ChangePasswordRequest;
import com.example.account.modules.facturation.dto.request.LoginRequest;
import com.example.account.modules.facturation.dto.request.PinLoginRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import reactor.core.publisher.Mono;

public interface AuthUseCase {
    Mono<SellerAuthResponse> login(LoginRequest request);
    Mono<SellerAuthResponse> loginByPin(PinLoginRequest request);
    Mono<SellerAuthResponse> changePassword(ChangePasswordRequest request);
    Mono<SellerAuthResponse> tryOut(LoginRequest request);
}
