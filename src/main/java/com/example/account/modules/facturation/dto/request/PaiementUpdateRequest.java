package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.enums.TypePaiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementUpdateRequest {

    private UUID idClient;

    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    private LocalDate date;

    private String journal;

    private TypePaiement modePaiement;

    private String compteBancaireF;

    private String memo;

    private UUID idFacture;
    private UUID organizationId;
    private UUID agencyId;
}