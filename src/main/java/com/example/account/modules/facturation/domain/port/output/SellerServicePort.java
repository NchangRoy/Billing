package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface SellerServicePort {
    Flux<SellerAuthResponse> getSellersByOrganization(UUID organizationId);
}
