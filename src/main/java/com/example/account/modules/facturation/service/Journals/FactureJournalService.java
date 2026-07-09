package com.example.account.modules.facturation.service.Journals;

import com.example.account.modules.facturation.dto.response.FactureResponse;
import com.example.account.modules.facturation.dto.response.SessionResponse;
import com.example.account.modules.facturation.domain.port.output.FactureRepositoryPort;
import com.example.account.modules.facturation.domain.port.output.SessionServicePort;
import com.example.account.modules.facturation.mapper.FactureMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FactureJournalService {

    private final FactureRepositoryPort factureRepository;
    private final FactureMapper factureMapper;
    private final SessionServicePort sessionService;

    /**
     * All invoices for an agency, each with its originating POS/SALES session
     * (see Facture.sessionId) embedded directly rather than left as a bare id.
     */
    public Flux<FactureResponse> enrichFacturesByAgency(UUID agencyId, UUID organizationId) {
        return sessionService.findAll(null, null, organizationId, null)
                .collectList()
                .flatMapMany(sessions -> factureRepository.findByAgencyId(agencyId)
                        .map(facture -> toEnrichedResponse(facture, sessions)));
    }

    /**
     * Same as {@link #enrichFacturesByAgency}, but organization-wide — for
     * journal/finance views that stack multiple agencies/sale points client-side
     * rather than issuing one request per agency.
     */
    public Flux<FactureResponse> enrichFacturesByOrganization(UUID organizationId) {
        return sessionService.findAll(null, null, organizationId, null)
                .collectList()
                .flatMapMany(sessions -> factureRepository.findByOrganizationId(organizationId)
                        .map(facture -> toEnrichedResponse(facture, sessions)));
    }

    private FactureResponse toEnrichedResponse(com.example.account.modules.facturation.domain.model.Facture facture, List<SessionResponse> sessions) {
        FactureResponse response = factureMapper.toResponse(facture);
        response.setSession(findMatchingSession(sessions, facture.getSessionId()));
        return response;
    }

    private SessionResponse findMatchingSession(List<SessionResponse> sessions, UUID sessionId) {
        if (sessionId == null) return null;
        return sessions.stream()
                .filter(s -> sessionId.equals(s.getId()))
                .findFirst()
                .orElse(null);
    }
}
