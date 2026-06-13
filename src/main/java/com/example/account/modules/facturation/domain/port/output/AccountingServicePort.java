package com.example.account.modules.facturation.domain.port.output;

import reactor.core.publisher.Mono;
import java.util.UUID;

public interface AccountingServicePort {
    Mono<Void> sendFactureData(UUID factureId);
}
