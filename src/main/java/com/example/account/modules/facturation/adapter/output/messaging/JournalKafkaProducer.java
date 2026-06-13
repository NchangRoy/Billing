package com.example.account.modules.facturation.adapter.output.messaging;

import com.example.account.modules.facturation.domain.port.output.JournalEventPort;
import com.example.account.modules.facturation.dto.response.JournalResponse;
import com.example.account.modules.facturation.service.producer.JournalEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JournalKafkaProducer implements JournalEventPort {

    private final JournalEventProducer originalProducer;

    @Override
    public void publishJournalCreated(JournalResponse journal) {
        originalProducer.publishJournalCreated(journal);
    }

    @Override
    public void publishJournalUpdated(JournalResponse journal) {
        originalProducer.publishJournalUpdated(journal);
    }

    @Override
    public void publishJournalDeleted(UUID journalId) {
        originalProducer.publishJournalDeleted(journalId);
    }
}
