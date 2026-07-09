package com.example.account.modules.facturation.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * idClient/organizationId are set server-side from the client-portal token,
 * never trusted from the request body — see PortalDataController.
 */
@Data
public class QuotationProposalCreateRequest {
    private UUID idClient;
    private String nomClient;
    private String emailClient;
    private UUID organizationId;
    private LocalDateTime dateValidite;
    private String commentary;
    private List<QuotationProposalLineRequest> lignesProposal;
    private Boolean applyVat;
    private String devise;
    private java.math.BigDecimal montantHT;
    private java.math.BigDecimal montantTVA;
    private java.math.BigDecimal montantTTC;
}
