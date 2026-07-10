package com.example.account.modules.facturation.domain.port.output;

import reactor.core.publisher.Mono;
import java.util.UUID;

public interface AccountingServicePort {
    Mono<Void> sendFactureData(UUID factureId);
    Mono<Void> sendFactureFournisseurData(UUID factureFournisseurId);

    // Called back by the accounting backend (via sales-core's forwarding
    // endpoints) once it has finished processing — flips the local facture
    // from ACCOUNT_PENDING to ACCOUNTED. No further accounting logic here.
    Mono<Void> markFactureAccounted(UUID factureId);
    Mono<Void> markFactureFournisseurAccounted(UUID factureFournisseurId);
}
