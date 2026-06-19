package com.example.account.modules.facturation.dto.response;

import com.example.account.modules.facturation.domain.model.LigneBackOrder;
import com.example.account.modules.facturation.model.enums.StatutBackOrder;
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
public class BackOrderResponse {

    private UUID idBackOrder;
    private String numeroBackOrder;

    private UUID idBonAchat;
    private String numeroBonAchat;

    private UUID idFournisseur;
    private String nomFournisseur;

    private List<LigneBackOrder> lignes;

    private LocalDateTime dateCreation;
    private LocalDateTime dateLivraisonPrevue;
    private LocalDateTime dateSysteme;

    private StatutBackOrder statut;
    private String notes;

    private UUID organizationId;
    private UUID agencyId;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
