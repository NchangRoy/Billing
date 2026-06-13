package com.example.account.modules.tiers.dto;

import com.example.account.modules.tiers.domain.model.enums.TypeClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FournisseurCreateRequest {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    private String username;

    @NotBlank(message = "La catégorie est obligatoire")
    private String categorie;

    private String siteWeb;

    @Builder.Default
    private Boolean nTva = false;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    private String telephone;

    @Email(message = "Format d'email invalide")
    private String email;

    @NotNull(message = "Le type de fournisseur est obligatoire")
    private TypeClient typeFournisseur;

    private String raisonSociale;

    private String numeroTva;

    private String codeFournisseur;

    private Double limiteCredit;

    @Builder.Default
    private Boolean actif = true;
}