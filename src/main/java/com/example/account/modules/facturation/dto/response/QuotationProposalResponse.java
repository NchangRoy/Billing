package com.example.account.modules.facturation.dto.response;

import com.example.account.modules.facturation.model.entity.LigneQuotationProposal;
import com.example.account.modules.facturation.model.enums.StatutQuotationProposal;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class QuotationProposalResponse {
    private UUID idProposal;
    private String numeroProposal;
    private UUID organizationId;
    private UUID idClient;
    private String nomClient;
    private String emailClient;
    private LocalDateTime dateCreation;
    private LocalDateTime dateValidite;
    private StatutQuotationProposal statut;
    private String commentary;
    private List<LigneQuotationProposal> lignesProposal;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private Boolean applyVat;
    private String devise;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
