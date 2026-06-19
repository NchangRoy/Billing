package com.example.account.modules.facturation.domain.model;

import com.example.account.modules.facturation.model.enums.ModeReglementNoteCredit;
import com.example.account.modules.facturation.model.enums.StatutNoteCredit;
import com.example.account.modules.core.domain.model.OrganizationScoped;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteCredit extends OrganizationScoped {

    private UUID idNoteCredit;
    private String numeroNoteCredit;
    private UUID idClient;
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;
    private UUID idFactureOrigine;
    private String numeroFactureOrigine;
    private List<LigneNoteCredit> lignesNoteCredit;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private BigDecimal montantTotal;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID agencyId;
}
