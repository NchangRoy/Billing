package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.response.PaiementResponse;

import java.util.UUID;

public interface PaiementEventPort {
    void publishPaiementCreated(PaiementResponse paiement);
    void publishPaiementUpdated(PaiementResponse paiement);
    void publishPaiementDeleted(UUID paiementId);
}
