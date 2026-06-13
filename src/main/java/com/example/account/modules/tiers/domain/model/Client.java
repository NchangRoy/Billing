package com.example.account.modules.tiers.domain.model;

import com.example.account.modules.core.domain.model.OrganizationScoped;
import com.example.account.modules.tiers.domain.model.enums.TypeClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client extends OrganizationScoped {

    private UUID idClient;
    private String username;
    private String categorie;
    private String siteWeb;
    @Builder.Default
    private Boolean nTva = false;
    private String adresse;
    private String telephone;
    private String email;
    private TypeClient typeClient;
    private String raisonSociale;
    private String numeroTva;
    private String codeClient;
    private Double limiteCredit;
    @Builder.Default
    private Double soldeCourant = 0.0;
    @Builder.Default
    private Boolean actif = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
