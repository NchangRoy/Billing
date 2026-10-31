package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Seller profile, role, permissions, and uniqueness now live purely locally
 * (see SellerLocalService) — Kernel's sales-core enforced email uniqueness
 * globally across every org on the platform, which caused real "already
 * taken" collisions. getSellersByOrganization is a different, narrower
 * Kernel "cashier" concept (used by DevisJournalService/DevisUseCaseImpl),
 * unrelated to the seller-admin CRUD this used to carry.
 */
public interface SellerServicePort {
    Flux<SellerAuthResponse> getSellersByOrganization(UUID organizationId);
}
