package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.response.FactureResponse;
import java.util.UUID;

public interface FactureEventPort {
    void publishFactureCreated(FactureResponse facture);
    void publishFactureUpdated(FactureResponse facture);
    void publishFactureDeleted(UUID factureId);
    void publishFacturePaid(FactureResponse facture);
}
