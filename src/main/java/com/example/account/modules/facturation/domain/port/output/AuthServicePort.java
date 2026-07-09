package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.request.ChangePasswordRequest;
import com.example.account.modules.facturation.dto.request.PinLoginRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AuthServicePort {
    Mono<SellerAuthResponse> login(String username, String password);
    Mono<SellerAuthResponse> loginByPin(PinLoginRequest request);
    Mono<SellerAuthResponse> changePassword(ChangePasswordRequest request);
    Mono<SellerAuthResponse> tryOut(String principal, String password, UUID organizationId);
}
