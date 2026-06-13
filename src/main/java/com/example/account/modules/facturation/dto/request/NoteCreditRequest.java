package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.enums.ModeReglementNoteCredit;
import com.example.account.modules.facturation.model.enums.StatutNoteCredit;
import com.example.account.modules.facturation.domain.model.LigneNoteCredit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteCreditRequest {
    // identifiers
    private String numeroNoteCredit;

    // client info
    private UUID idClient;
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;

    // origin invoice reference
    private UUID idFactureOrigine;
    private String numeroFactureOrigine;

    // line items (reuse entity type for now)
    private List<LigneNoteCredit> lignesNoteCredit;

    // amounts
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal montantTotal;

    // dates
    private LocalDateTime dateEmission;
    private LocalDateTime dateSysteme;

    private ModeReglementNoteCredit modeReglement;
    private StatutNoteCredit statut;
    private String motif;

    private String notes;
    private String devise;
    private String pdfPath;

    private UUID createdBy;
    private UUID validatedBy;
    private LocalDateTime validatedAt;

    private UUID organizationId;
}
