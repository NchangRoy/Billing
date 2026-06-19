package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.enums.TypePaiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementCreateRequest {

    @NotNull(message = "L'ID client est obligatoire")
    private UUID idClient;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotNull(message = "Le journal est obligatoire")
    private String journal;

    @NotNull(message = "Le mode de paiement est obligatoire")
    private TypePaiement modePaiement;

    private String compteBancaireF;

    private String memo;

    private UUID idFacture;
    private UUID organizationId;
    private UUID agencyId;
}