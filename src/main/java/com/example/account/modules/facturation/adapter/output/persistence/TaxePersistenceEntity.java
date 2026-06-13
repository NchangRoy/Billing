package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.core.adapter.output.persistence.OrganizationScopedPersistenceEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("taxes")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxePersistenceEntity extends OrganizationScopedPersistenceEntity {

    @Id
    @Column("id_taxe")
    private UUID idTaxe;

    @Column("nom_taxe")
    private String nomTaxe;

    @Column("calcul_taxe")
    private BigDecimal calculTaxe;

    @Column("actif")
    @Builder.Default
    private Boolean actif = true;

    @Column("type_taxe")
    private String typeTaxe;

    @Column("porte_taxe")
    private String porteTaxe;

    @Column("montant")
    private BigDecimal montant;

    @Column("position_fiscale")
    private String positionFiscale;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
