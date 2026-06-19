package com.example.account.modules.facturation.dto.request;

import com.example.account.modules.facturation.model.enums.StatutBonAchat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonAchatRequest {

    @NotBlank(message = "Le numéro de bon d'achat est obligatoire")
    private String numeroBonAchat;

    // --- Informations Fournisseur ---
    @NotNull(message = "L'ID fournisseur est obligatoire")
    private UUID supplierId;
    
    private String supplierName;
    private String supplierCode;
    private String supplierEmail;
    private String supplierContact;
    private String supplierAddress;

    // --- Informations de Livraison ---
    private String deliveryName;
    private String deliveryAddress;
    private String deliveryEmail;
    private String deliveryContact;

    // --- Dates ---
    private LocalDateTime dateBonAchat;
    private LocalDateTime dateSysteme;
    private LocalDateTime dateLivraisonPrevue;

    // --- Transport & Statut ---
    private String transportMethod;
    private String instructionsLivraison;
    
    @NotNull(message = "Le statut est obligatoire")
    private StatutBonAchat status;

    // --- Totaux ---
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;

    // --- Audit & Remarques ---
    private UUID preparedBy;
    private UUID approvedBy;
    private String remarks;
    private UUID organizationId;
    private UUID agencyId;
    private UUID createdBy;

    // --- Lignes ---
    @Valid
    @NotNull(message = "Les lignes du bon d'achat ne peuvent pas être vides")
    private List<LigneBonAchatRequest> lines;
}