package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.response.TaxeResponse;

import java.util.UUID;

public interface TaxeEventPort {
    void publishTaxeCreated(TaxeResponse taxe);
    void publishTaxeUpdated(TaxeResponse taxe);
    void publishTaxeDeleted(UUID taxeId);
}
