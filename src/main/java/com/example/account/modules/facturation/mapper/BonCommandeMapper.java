package com.example.account.modules.facturation.mapper;

import com.example.account.modules.facturation.dto.request.BonCommandeCreateRequest;

import com.example.account.modules.facturation.dto.response.BonCommandeResponse;
import com.example.account.modules.facturation.model.entity.BonCommande;
import com.example.account.modules.facturation.model.entity.Devis;
import com.example.account.modules.facturation.model.entity.Lines.LineBonCommande;
import com.example.account.modules.facturation.model.enums.StatusBonCommande;

import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BonCommandeMapper {

    /**
     * Convertit la requête de création en Entité.
     * Les champs manquants (id, createdAt, etc.) sont gérés par JPA (@GeneratedValue, @PrePersist).
     */
    @Mapping(target = "idBonCommande", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    
    BonCommande toEntity(BonCommandeCreateRequest request);

    /**
     * Met à jour une entité existante à partir d'une requête.
     * Utile pour les méthodes PUT.
     */
    @Mapping(target = "idBonCommande", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    void updateEntityFromRequest(BonCommandeCreateRequest request, @MappingTarget BonCommande entity);

    /**
     * Convertit l'Entité en Réponse DTO pour l'API.
     */
    BonCommandeResponse toResponse(BonCommande entity);

    /**
     * Convertit une liste d'entités en liste de réponses.
     */
    List<BonCommandeResponse> toResponseList(List<BonCommande> entities);


    default BonCommande mapDevisToBonCommande(Devis devis) {
        if (devis == null) {
            return null;
        }

        // 1. Mapping des lignes
       List<LineBonCommande> lines = devis.getLignesDevis().stream()
    .<LineBonCommande>map(ligne -> LineBonCommande.builder() // Add <LineBonCommande> here
        .idProduit(ligne.getIdProduit())
        .nomProduit(ligne.getNomProduit())
        .description(ligne.getDescription())
        .quantite(ligne.getQuantite())
        .prixUnitaire(ligne.getPrixUnitaire())
        .montantTotal(ligne.getMontantTotal())
        
        .remisePourcentage(ligne.getRemisePourcentage())
        .remiseMontant(ligne.getRemiseMontant())
        .build())
    .collect(Collectors.toList());

        // 2. Construction de l'entité via Builder
        BonCommande bonCommande = BonCommande.builder()
            .idBonCommande(UUID.randomUUID()) 
            .idClient(devis.getIdClient())
            .nomClient(devis.getNomClient())
            .adresseClient(devis.getAdresseClient())
            .emailClient(devis.getEmailClient())
            .telephoneClient(devis.getTelephoneClient())
            
            .idDevisOrigine(devis.getIdDevis())
            .numeroDevisOrigine(devis.getNumeroDevis())
            
            .lines(lines)
            .montantHT(devis.getMontantHT())
            .montantTVA(devis.getMontantTVA())
            .montantTTC(devis.getMontantTTC())
            .devise(devis.getDevise())
            .applyVat(devis.getApplyVat())
            
            .nosRef(devis.getNosRef())
            .vosRef(devis.getVosRef())
            .notes(devis.getNotes())
            .dateCommande(LocalDateTime.now())
            .dateSysteme(LocalDateTime.now())
            .statut(StatusBonCommande.BROUILLON)
            
            .createdBy(devis.getCreatedBy())
            .build();
            
        bonCommande.setOrganizationId(devis.getOrganizationId());
        
        return bonCommande;
    }
}