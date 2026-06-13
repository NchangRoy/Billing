package com.example.account.modules.facturation.adapter.output.messaging;

import com.example.account.modules.facturation.domain.port.output.TaxeEventPort;
import com.example.account.modules.facturation.dto.response.TaxeResponse;
import com.example.account.modules.facturation.service.producer.TaxeEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TaxeKafkaProducer implements TaxeEventPort {

    private final TaxeEventProducer originalProducer;

    @Override
    public void publishTaxeCreated(TaxeResponse taxe) {
        originalProducer.publishTaxeCreated(taxe);
    }

    @Override
    public void publishTaxeUpdated(TaxeResponse taxe) {
        originalProducer.publishTaxeUpdated(taxe);
    }

    @Override
    public void publishTaxeDeleted(UUID taxeId) {
        originalProducer.publishTaxeDeleted(taxeId);
    }
}
