package com.example.account.modules.facturation.domain.model;

import com.example.account.modules.core.domain.model.OrganizationScoped;
import com.example.account.modules.facturation.model.enums.StatusBonCommande;
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
public class BonCommande extends OrganizationScoped {

    private UUID idBonCommande;
    private String numeroCommande;
    private UUID idClient;
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String recipientCity;
    private UUID idDevisOrigine;
    private String numeroDevisOrigine;
    private String nosRef;
    private String vosRef;
    private LocalDateTime dateCommande;
    private LocalDateTime dateSysteme;
    private LocalDateTime dateLivraisonPrevue;
    private List<LineBonCommande> lines;
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;
    private String devise;
    private Boolean applyVat;
    private String transportMethod;
    private UUID idAgency;
    private String modeReglement;
    private StatusBonCommande statut;
    private String notes;
    private UUID createdBy;
    private UUID validatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime validatedAt;
}
