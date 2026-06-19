package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.enums.StatutBonLivraison;
import jakarta.validation.constraints.NotNull;
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
public class BonLivraisonRequest {

    // Simple identifier for client-facing usage
    private String numeroBonLivraison;

    private UUID idSaleOrder;
    private String saleOrderNumber;
    private boolean isPartial;

    @NotNull(message = "L'ID client est obligatoire")
    private UUID idClient;

    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;

    private String nomAgence;
    private String adresseAgence;
    private String contactAgence;

    @NotNull(message = "La date de livraison est obligatoire")
    private LocalDateTime dateLivraison;

    private LocalDateTime dateEcheance;
    private UUID idFacture;
    private String numeroFacture;
    private UUID idBonCommande;
    private String numeroCommande;
    private StatutBonLivraison statut;

    @NotNull(message = "Les lignes de livraison ne peuvent pas être vides")
    @com.fasterxml.jackson.annotation.JsonAlias({"lines","lignes"})
    private List<LigneBonLivraisonRequest> lignes;

    // Amounts (match entity)
    private BigDecimal montantHT;
    private BigDecimal montantTVA;
    private BigDecimal montantTTC;

    

    private String notes;
    private String transporteur;
    private String numeroSuivi;

     private UUID createdBy;
    private LocalDateTime dateSysteme;
    private UUID organizationId;
    private UUID agencyId;
}
