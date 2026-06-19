package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.entity.Lines.LineBonReception;
import com.example.account.modules.facturation.model.enums.StatusBonReception;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BondeReceptionCreateRequest {

 

    // Fields aligned with entity `BondeReception`
   
    private String numeroReception;
    private UUID idFournisseur;
    private String nomFournisseur;
    private List<LineBonReception> lines;
    private LocalDateTime dateReception;
    private StatusBonReception statut;
    private String notes;
    private UUID createdBy;
    private LocalDateTime dateSysteme;
    private String numeroBonAchat;
    private UUID idBonAchat;
    private String agenceDeTransport;
    private UUID organizationId;
    private UUID agencyId;
} 
