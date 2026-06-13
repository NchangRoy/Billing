package com.example.account.modules.tiers.dto;

import com.example.account.modules.tiers.domain.model.enums.TypeClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponse {

    private UUID idClient;
    private String username;
    private String categorie;
    private String siteWeb;
    private Boolean nTva;
    private String adresse;
    private String telephone;
    private String email;
    private TypeClient typeClient;
    private String raisonSociale;
    private String numeroTva;
    private String codeClient;
    private Double limiteCredit;
    private Double soldeCourant;
    private Boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}