package com.example.account.modules.facturation.domain.model;

import com.example.account.modules.core.domain.model.OrganizationScoped;
import com.example.account.modules.facturation.model.enums.StatutBackOrder;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackOrder extends OrganizationScoped {

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

    private UUID agencyId;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
