package com.example.account.modules.facturation.model.enums;

import lombok.Getter;

@Getter
public enum StatutBonAchat {
    BROUILLON("Brouillon"),
    ENVOYE("Envoyé"),
    ACCEPTE("Accepté"),
    RECU_PARTIEL("Reçu partiellement"),
    RECU("Reçu"),
    REJETE("Rejeté"),
    ANNULE("Annulé");

    private final String libelle;

    StatutBonAchat(String libelle) {
        this.libelle = libelle;
    }
}
