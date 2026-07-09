package com.example.account.modules.shared.dto.kernel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Matches sales-core's UpdatedClientResponse — returned by both its
 * /api/customers and /api/fournisseurs (sales-core augments Kernel's raw
 * third-party data with allowedSaleSizes/ntva, which Kernel itself has no
 * concept of). Used for both clients and fournisseurs here too.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesCoreClientResponse {
    private UUID idClient;
    private String username;
    private String categorie;
    private String siteWeb;
    private String adresse;
    private String telephone;
    private String email;
    private String typeClient;
    private String raisonSociale;
    private String numeroTva;
    private String codeClient;
    private Double limiteCredit;
    private Double soldeCourant;
    private Boolean actif;
    private String createdAt;
    private String updatedAt;
    private Boolean ntva;
    private List<String> allowedSaleSizes;
}
