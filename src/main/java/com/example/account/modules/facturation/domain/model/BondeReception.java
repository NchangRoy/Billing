package com.example.account.modules.facturation.domain.model;

import com.example.account.modules.facturation.model.enums.StatusBonReception;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BondeReception {

    private UUID idBonReception;
    private String numeroReception;
    private UUID idFournisseur;
    private String nomFournisseur;
    private List<LineBonReception> lines;
    private LocalDateTime dateReception;
    private StatusBonReception statut;
    private String notes;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private LocalDateTime dateSysteme;
    private String numeroBonAchat;
    private UUID idBonAchat;
    private String agenceDeTransport;
    private UUID organizationId;
}
