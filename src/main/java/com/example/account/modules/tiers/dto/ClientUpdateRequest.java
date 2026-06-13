package com.example.account.modules.tiers.dto;

import com.example.account.modules.tiers.domain.model.enums.TypeClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientUpdateRequest {

    private String username;
    private String categorie;
    private String siteWeb;
    private Boolean nTva;
    private String adresse;
    private String telephone;

    @Email(message = "Format d'email invalide")
    private String email;

    private TypeClient typeClient;
    private String raisonSociale;
    private String numeroTva;
    private String codeClient;
    private Double limiteCredit;
    private Boolean actif;
}