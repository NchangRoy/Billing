package com.example.account.modules.facturation.domain.model;

import com.example.account.modules.core.domain.model.OrganizationScoped;
import com.example.account.modules.facturation.model.enums.TypePaiement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement extends OrganizationScoped {

    private UUID idPaiement;
    private UUID idClient;
    private BigDecimal montant;
    private LocalDate date;
    private String journal;
    private TypePaiement modePaiement;
    private String compteBancaireF;
    private String memo;
    private UUID idFacture;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
