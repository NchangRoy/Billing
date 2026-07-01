package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import reactor.core.publisher.Mono;

public interface AuthServicePort {
    Mono<SellerAuthResponse> login(String username, String password);
}
