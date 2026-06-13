package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.response.DevisResponse;

import java.util.UUID;

public interface DevisEventPort {
    void publishDevisCreated(DevisResponse devis);
    void publishDevisUpdated(DevisResponse devis);
    void publishDevisDeleted(UUID devisId);
    void publishDevisAccepted(DevisResponse devis);
}
