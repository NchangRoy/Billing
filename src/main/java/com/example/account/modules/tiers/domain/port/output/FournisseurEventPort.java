package com.example.account.modules.tiers.domain.port.output;

import com.example.account.modules.tiers.dto.FournisseurResponse;

import java.util.UUID;

public interface FournisseurEventPort {
    void publishFournisseurCreated(FournisseurResponse fournisseur);
    void publishFournisseurUpdated(FournisseurResponse fournisseur);
    void publishFournisseurDeleted(UUID fournisseurId);
}
