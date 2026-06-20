package com.example.account.modules.facturation.adapter.output.external;

import com.example.account.modules.facturation.domain.port.output.ProductServicePort;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse.SaleSize;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse.SaleSizePromotion;
import com.example.account.modules.facturation.service.ExternalServices.entity.enums.SaleSizeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ProductServiceAdapter implements ProductServicePort {

    @Override
    public Flux<ProductResponse> fetchProductsByOrganization(UUID organizationId) {
        log.info("Fetching mock products for organization: {}", organizationId);
        return Flux.fromIterable(mockProducts()).filter(p -> organizationId.equals(p.getOrganizationId()));
    }

    @Override
    public Flux<ProductResponse> fetchAllProducts() {
        log.info("Fetching all mock products");
        return Flux.fromIterable(mockProducts());
    }

    private List<ProductResponse> mockProducts() {
        UUID orgId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        return List.of(
            ProductResponse.builder()
                .idProduit(UUID.fromString("11111111-0000-0000-0000-000000000001"))
                .nomProduit("Stylo Bic")
                .typeProduit("Consommable")
                .prixVente(new BigDecimal("150"))
                .cout(new BigDecimal("80"))
                .categorie("Papeterie")
                .reference("STY-001")
                .codeBarre("1234567890001")
                .active(true)
                .stockQuantity(500.0)
                .availableQuantity(500.0)
                .reservedQuantity(0.0)
                .uom("Pièce")
                .organizationId(orgId)
                .allowedSaleSizes(List.of(
                    new SaleSize(SaleSizeType.DETAIL, new BigDecimal("150"), new BigDecimal("172.5"), 1, true, false, null),
                    new SaleSize(SaleSizeType.GROS, new BigDecimal("1500"), new BigDecimal("1725"), 12, true, true, 5.0)
                ))
                .activePromotions(List.of())
                .build(),

            ProductResponse.builder()
                .idProduit(UUID.fromString("11111111-0000-0000-0000-000000000002"))
                .nomProduit("Cahier 200 pages")
                .typeProduit("Consommable")
                .prixVente(new BigDecimal("800"))
                .cout(new BigDecimal("500"))
                .categorie("Papeterie")
                .reference("CAH-200")
                .codeBarre("1234567890002")
                .active(true)
                .stockQuantity(300.0)
                .availableQuantity(290.0)
                .reservedQuantity(10.0)
                .uom("Pièce")
                .organizationId(orgId)
                .allowedSaleSizes(List.of(
                    new SaleSize(SaleSizeType.DETAIL, new BigDecimal("800"), new BigDecimal("920"), 1, true, false, null),
                    new SaleSize(SaleSizeType.DEMI_GROS, new BigDecimal("3500"), new BigDecimal("4025"), 5, true, true, 8.0),
                    new SaleSize(SaleSizeType.GROS, new BigDecimal("7000"), new BigDecimal("8050"), 10, true, true, 10.0)
                ))
                .activePromotions(List.of(
                    new SaleSizePromotion(SaleSizeType.DETAIL, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30),
                            new BigDecimal("700"), 12.5, true)
                ))
                .build(),

            ProductResponse.builder()
                .idProduit(UUID.fromString("11111111-0000-0000-0000-000000000003"))
                .nomProduit("Imprimante HP LaserJet")
                .typeProduit("Equipement")
                .prixVente(new BigDecimal("85000"))
                .cout(new BigDecimal("60000"))
                .categorie("Informatique")
                .reference("IMP-HP-001")
                .codeBarre("1234567890003")
                .active(true)
                .stockQuantity(20.0)
                .availableQuantity(18.0)
                .reservedQuantity(2.0)
                .uom("Unité")
                .organizationId(orgId)
                .allowedSaleSizes(List.of(
                    new SaleSize(SaleSizeType.DETAIL, new BigDecimal("85000"), new BigDecimal("97750"), 1, true, true, 8.0)
                ))
                .activePromotions(List.of())
                .build()
        );
    }
}
