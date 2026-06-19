package com.example.account.modules.facturation.model.enums;

import lombok.Getter;

@Getter
public enum StatutBonLivraison {
    EN_PREPARATION("En préparation"),
    PRET_A_EXPEDIER("Prêt à expédier"),
    EXPEDIE("Expédié"),
    LIVRE("Livré"),
    PARTIELLE("Partielle"),
    RETOURNE("Retourné"),
    ANNULE("Annulé");

    private final String libelle;

    StatutBonLivraison(String libelle) {
        this.libelle = libelle;
    }
}
