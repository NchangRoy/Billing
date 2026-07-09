package com.example.account.modules.facturation.dto.response;

import com.example.account.modules.facturation.model.enums.StatutBonAchat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonAchatResponse {
    private UUID idBonAchat;
    private String numeroBonAchat;

    // --- Informations Fournisseur ---
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

    // --- Dates (Harmonisation en LocalDateTime) ---
    private LocalDateTime dateBonAchat;
    private LocalDateTime dateSysteme;
    private LocalDateTime dateLivraisonPrevue;

    // --- Transport & Statut ---
    private String transportMethod;
    private String instructionsLivraison;
    private StatutBonAchat status;

    // --- Lignes ---
    private List<LigneBonAchatResponse> lines;

    // --- Totaux (Manquants dans votre version précédente) ---
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;

    // --- Audit & Métadonnées ---
    private UUID preparedBy;
    private UUID approvedBy;
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID organizationId;
    private UUID agencyId;
    private UUID createdBy;
    private DocPermissionResponse docPermission;
}