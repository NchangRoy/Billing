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

    @NotNull(message = "L'ID du bon d'achat est obligatoire")
    private UUID idBonAchat;
    private String numeroBonAchat;

    private UUID idFournisseur;
    private String nomFournisseur;

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
