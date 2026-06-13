package com.example.account.modules.facturation.adapter.output.messaging;

import com.example.account.modules.facturation.domain.port.output.PaiementEventPort;
import com.example.account.modules.facturation.dto.response.PaiementResponse;
import com.example.account.modules.facturation.service.producer.PaiementEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaiementKafkaProducer implements PaiementEventPort {

    private final PaiementEventProducer originalProducer;

    @Override
    public void publishPaiementCreated(PaiementResponse paiement) {
        originalProducer.publishPaiementCreated(paiement);
    }

    @Override
    public void publishPaiementUpdated(PaiementResponse paiement) {
        originalProducer.publishPaiementUpdated(paiement);
    }

    @Override
    public void publishPaiementDeleted(UUID paiementId) {
        originalProducer.publishPaiementDeleted(paiementId);
    }
}
