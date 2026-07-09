package com.example.account.modules.facturation.model.enums;

import lombok.Getter;

@Getter
public enum TypeNumerotation {
    FACTURE("Facture"),
    DEVIS("Devis"),
    AVOIR("Avoir"),
    PAIEMENT("Paiement"),
    REMBOURSEMENT("Remboursement"),
    COMMANDE("Commande"),
    BON_LIVRAISON("Bon de livraison"),
    PROFORMA("Proforma Invoice"),
    SALES_ORDER("Sales Order"),
    PURCHASE_ORDER("Purchase Order"),
    BACK_ORDER("Back Order"),
    GOODS_RECEIPT("Goods Receipt Note"),
    SUPPLIER_INVOICE("Supplier Invoice"),
    CLIENT("Client"),
    FOURNISSEUR("Fournisseur"),
    PRODUIT("Produit"),
    ABONNEMENT("Abonnement"),
    CONTRAT("Contrat"),
    PROJET("Projet"),
    TICKET("Ticket"),
    PERSONNALISE("Personnalisé");

    private final String libelle;

    TypeNumerotation(String libelle) {
        this.libelle = libelle;
    }
}