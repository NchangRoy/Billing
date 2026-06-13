package com.example.account.modules.facturation.domain.port.input;

import com.example.account.modules.facturation.dto.request.JournalCreateRequest;
import com.example.account.modules.facturation.dto.request.JournalUpdateRequest;
import com.example.account.modules.facturation.dto.response.JournalResponse;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface JournalUseCase {
    Mono<JournalResponse> createJournal(JournalCreateRequest request);
    Mono<JournalResponse> updateJournal(UUID journalId, JournalUpdateRequest request);
    Mono<JournalResponse> getJournalById(UUID journalId);
    Mono<JournalResponse> getJournalByNom(String nomJournal);
    Flux<JournalResponse> getAllJournals();
    Flux<JournalResponse> getAllJournals(Pageable pageable);
    Flux<JournalResponse> getJournalsByType(String type);
    Flux<JournalResponse> searchJournalsByNom(String nomJournal);
    Mono<Void> deleteJournal(UUID journalId);
    Mono<Long> countByType(String type);
}
