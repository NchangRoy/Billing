package com.example.account.modules.facturation.mapper;

import com.example.account.modules.facturation.dto.request.BonCommandeCreateRequest;

import com.example.account.modules.facturation.dto.response.BonCommandeResponse;
import com.example.account.modules.facturation.model.entity.BonCommande;
import com.example.account.modules.facturation.domain.model.Devis;
import com.example.account.modules.facturation.model.entity.Lines.LineBonCommande;
import com.example.account.modules.facturation.model.enums.StatusBonCommande;

import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    builder = @org.mapstruct.Builder(disableBuilder = true)
)
public interface BonCommandeMapper {

    @Mapping(target = "idBonCommande", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "agencyId", target = "idAgency")
    BonCommande toEntity(BonCommandeCreateRequest request);

    @Mapping(target = "idBonCommande", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(source = "agencyId", target = "idAgency")
    void updateEntityFromRequest(BonCommandeCreateRequest request, @MappingTarget BonCommande entity);

    @Mapping(source = "idAgency", target = "agencyId")
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