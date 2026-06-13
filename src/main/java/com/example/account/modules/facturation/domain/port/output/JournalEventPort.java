package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.response.JournalResponse;

import java.util.UUID;

public interface JournalEventPort {
    void publishJournalCreated(JournalResponse journal);
    void publishJournalUpdated(JournalResponse journal);
    void publishJournalDeleted(UUID journalId);
}
