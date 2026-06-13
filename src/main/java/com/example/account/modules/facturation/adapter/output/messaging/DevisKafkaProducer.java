package com.example.account.modules.facturation.adapter.output.messaging;

import com.example.account.modules.core.service.KafkaProducerService;
import com.example.account.modules.facturation.domain.port.output.DevisEventPort;
import com.example.account.modules.facturation.dto.response.DevisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DevisKafkaProducer implements DevisEventPort {

    private final KafkaProducerService kafkaProducerService;

    private static final String DEVIS_CREATED_TOPIC = "devis-created";
    private static final String DEVIS_UPDATED_TOPIC = "devis-updated";
    private static final String DEVIS_DELETED_TOPIC = "devis-deleted";
    private static final String DEVIS_ACCEPTED_TOPIC = "devis-accepted";

    @Override
    public void publishDevisCreated(DevisResponse devisResponse) {
        log.info("Publication de l'événement devis créé: {}", devisResponse.getNumeroDevis());
        kafkaProducerService.sendMessage(DEVIS_CREATED_TOPIC, devisResponse.getIdDevis().toString(), devisResponse);
    }

    @Override
    public void publishDevisUpdated(DevisResponse devisResponse) {
        log.info("Publication de l'événement devis mis à jour: {}", devisResponse.getNumeroDevis());
        kafkaProducerService.sendMessage(DEVIS_UPDATED_TOPIC, devisResponse.getIdDevis().toString(), devisResponse);
    }

    @Override
    public void publishDevisDeleted(UUID devisId) {
        log.info("Publication de l'événement devis supprimé: {}", devisId);
        kafkaProducerService.sendMessage(DEVIS_DELETED_TOPIC, devisId.toString(), devisId);
    }

    @Override
    public void publishDevisAccepted(DevisResponse devisResponse) {
        log.info("Publication de l'événement devis accepté: {}", devisResponse.getNumeroDevis());
        kafkaProducerService.sendMessage(DEVIS_ACCEPTED_TOPIC, devisResponse.getIdDevis().toString(), devisResponse);
    }
}
