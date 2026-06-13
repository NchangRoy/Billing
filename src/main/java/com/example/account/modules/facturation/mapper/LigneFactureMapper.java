package com.example.account.modules.facturation.mapper;

import com.example.account.modules.facturation.dto.request.LigneFactureCreateRequest;
import com.example.account.modules.facturation.dto.response.LigneFactureResponse;
import com.example.account.modules.facturation.domain.model.LigneFacture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface LigneFactureMapper {

  
    @Mapping(target = "montantTotal", expression = "java(calculateMontantTotal(createRequest.getQuantite(), createRequest.getPrixUnitaire()))")
    LigneFacture toEntity(LigneFactureCreateRequest createRequest);

    LigneFactureResponse toResponse(LigneFacture ligneFacture);

    List<LigneFacture> toEntityList(List<LigneFactureCreateRequest> createRequests);

    List<LigneFactureResponse> toResponseList(List<LigneFacture> lignesFacture);

    // Méthode utilitaire pour calculer le montant total
    default BigDecimal calculateMontantTotal(Integer quantite, BigDecimal prixUnitaire) {
        if (quantite == null || prixUnitaire == null) {
            return BigDecimal.ZERO;
        }
        return prixUnitaire.multiply(new BigDecimal(quantite));
    }
}