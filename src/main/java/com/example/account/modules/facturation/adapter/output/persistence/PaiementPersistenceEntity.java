package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.core.adapter.output.persistence.OrganizationScopedPersistenceEntity;
import com.example.account.modules.facturation.model.enums.TypePaiement;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("paiements")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementPersistenceEntity extends OrganizationScopedPersistenceEntity {

    @Id
    @Column("id_paiement")
    private UUID idPaiement;

    @Column("id_client")
    private UUID idClient;

    @Column("montant")
    private BigDecimal montant;

    @Column("date")
    private LocalDate date;

    @Column("journal")
    private String journal;

    @Column("mode_paiement")
    private TypePaiement modePaiement;

    @Column("compte_bancaire_f")
    private String compteBancaireF;

    @Column("memo")
    private String memo;

    @Column("id_facture")
    private UUID idFacture;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
