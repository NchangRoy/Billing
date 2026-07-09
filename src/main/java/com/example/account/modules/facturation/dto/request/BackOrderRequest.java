package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.domain.model.LigneBackOrder;
import com.example.account.modules.facturation.model.enums.StatutBackOrder;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackOrderRequest {

    private String numeroBackOrder;

    @NotNull(message = "L'ID du bon de livraison est obligatoire")
    private UUID idBonLivraison;
    private String numeroBonLivraison;

    private UUID idClient;
    private String nomClient;
    private String adresseClient;
    private String emailClient;
    private String telephoneClient;

    @NotNull(message = "Les lignes sont obligatoires")
    private List<LigneBackOrder> lignes;

    private LocalDateTime dateCreation;
    private LocalDateTime dateLivraisonPrevue;
    private LocalDateTime dateSysteme;

    private StatutBackOrder statut;
    private String notes;

    private UUID organizationId;
    private UUID agencyId;
    private UUID createdBy;
}
