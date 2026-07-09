package com.example.account.modules.portal.controller;

import com.example.account.modules.facturation.domain.port.input.DevisUseCase;
import com.example.account.modules.facturation.domain.port.input.FactureUseCase;
import com.example.account.modules.facturation.dto.request.QuotationProposalCreateRequest;
import com.example.account.modules.facturation.dto.response.BonAchatResponse;
import com.example.account.modules.facturation.dto.response.DevisResponse;
import com.example.account.modules.facturation.dto.response.FactureFournisseurResponse;
import com.example.account.modules.facturation.dto.response.FactureResponse;
import com.example.account.modules.facturation.dto.response.QuotationProposalResponse;
import com.example.account.modules.facturation.service.BonAchatService;
import com.example.account.modules.facturation.service.FactureFournisseurService;
import com.example.account.modules.facturation.service.QuotationProposalService;
import com.example.account.modules.portal.security.PortalTokenVerifier;
import com.example.account.modules.settings.service.SettingService;
import com.example.account.modules.tiers.domain.port.input.ClientUseCase;
import com.example.account.modules.tiers.dto.ClientResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Read-only document access for the client-portal (see PortalAuthController).
 * The caller authenticates with a client-portal JWT — never a seller token —
 * and only ever sees documents matching their own thirdPartyId, resolved from
 * the token, never from a client-supplied id.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class PortalDataController {

    private final PortalTokenVerifier tokenVerifier;
    private final DevisUseCase devisService;
    private final FactureUseCase factureService;
    private final BonAchatService bonAchatService;
    private final FactureFournisseurService factureFournisseurService;
    private final QuotationProposalService quotationProposalService;
    private final ClientUseCase clientUseCase;
    private final SettingService settingService;

    @Qualifier("salesCoreWebClient")
    private final WebClient salesCoreWebClient;

    private PortalTokenVerifier.PortalPrincipal authenticate(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing portal token");
        }
        try {
            return tokenVerifier.verify(authHeader.substring(7));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    private void requireRole(PortalTokenVerifier.PortalPrincipal principal, String role) {
        if (!role.equalsIgnoreCase(principal.partyRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This document type isn't available for your account type");
        }
    }

    @GetMapping("/api/portal/quotations")
    public Flux<DevisResponse> getMyQuotations(@RequestHeader("Authorization") String authHeader) {
        var principal = authenticate(authHeader);
        requireRole(principal, "CUSTOMER");
        String clientId = principal.clientId().toString();
        return devisService.getAllDevis()
                .filter(d -> clientId.equalsIgnoreCase(d.getIdClient()))
                .filter(d -> principal.organizationId().equals(d.getOrganizationId()));
    }

    @GetMapping("/api/portal/invoices")
    public Flux<FactureResponse> getMyInvoices(@RequestHeader("Authorization") String authHeader) {
        var principal = authenticate(authHeader);
        requireRole(principal, "CUSTOMER");
        String clientId = principal.clientId().toString();
        return factureService.getAllFactures()
                .filter(f -> clientId.equalsIgnoreCase(f.getIdClient()))
                .filter(f -> principal.organizationId().equals(f.getOrganizationId()));
    }

    @GetMapping("/api/portal/purchase-orders")
    public Flux<BonAchatResponse> getMyPurchaseOrders(@RequestHeader("Authorization") String authHeader) {
        var principal = authenticate(authHeader);
        requireRole(principal, "SUPPLIER");
        return bonAchatService.getAllBonsAchat()
                .filter(bo -> principal.clientId().equals(bo.getSupplierId()))
                .filter(bo -> principal.organizationId().equals(bo.getOrganizationId()));
    }

    @GetMapping("/api/portal/supplier-invoices")
    public Flux<FactureFournisseurResponse> getMySupplierInvoices(@RequestHeader("Authorization") String authHeader) {
        var principal = authenticate(authHeader);
        requireRole(principal, "SUPPLIER");
        return factureFournisseurService.getAllFactures()
                .filter(fi -> principal.clientId().equals(fi.getIdFournisseur()))
                .filter(fi -> principal.organizationId().equals(fi.getOrganizationId()));
    }

    @PostMapping("/api/portal/quotations/{id}/accept")
    public Mono<ResponseEntity<Void>> acceptQuotation(@PathVariable UUID id, @RequestHeader("Authorization") String authHeader) {
        var principal = authenticate(authHeader);
        requireRole(principal, "CUSTOMER");
        return devisService.getDevisById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Quotation not found")))
                .flatMap(d -> {
                    requireOwnership(principal, d.getIdClient(), d.getOrganizationId());
                    return devisService.accepterDevis(id);
                })
                .thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/api/portal/quotations/{id}/reject")
    public Mono<ResponseEntity<Void>> rejectQuotation(@PathVariable UUID id, @RequestHeader("Authorization") String authHeader) {
        var principal = authenticate(authHeader);
        requireRole(principal, "CUSTOMER");
        return devisService.getDevisById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Quotation not found")))
                .flatMap(d -> {
                    requireOwnership(principal, d.getIdClient(), d.getOrganizationId());
                    return devisService.refuserDevis(id);
                })
                .thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/api/portal/purchase-orders/{id}/accept")
    public Mono<ResponseEntity<Void>> acceptPurchaseOrder(@PathVariable UUID id, @RequestHeader("Authorization") String authHeader) {
        var principal = authenticate(authHeader);
        requireRole(principal, "SUPPLIER");
        return bonAchatService.getBonAchatById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found")))
                .flatMap(bo -> {
                    requireOwnership(principal, bo.getSupplierId() != null ? bo.getSupplierId().toString() : null, bo.getOrganizationId());
                    return bonAchatService.accepterBonAchat(id);
                })
                .thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/api/portal/purchase-orders/{id}/reject")
    public Mono<ResponseEntity<Void>> rejectPurchaseOrder(@PathVariable UUID id, @RequestHeader("Authorization") String authHeader) {
        var principal = authenticate(authHeader);
        requireRole(principal, "SUPPLIER");
        return bonAchatService.getBonAchatById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found")))
                .flatMap(bo -> {
                    requireOwnership(principal, bo.getSupplierId() != null ? bo.getSupplierId().toString() : null, bo.getOrganizationId());
                    return bonAchatService.refuserBonAchat(id);
                })
                .thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/api/portal/quotation-proposals")
    public Mono<QuotationProposalResponse> proposeQuotation(@RequestHeader("Authorization") String authHeader,
                                                             @Valid @RequestBody QuotationProposalCreateRequest request) {
        var principal = authenticate(authHeader);
        requireRole(principal, "CUSTOMER");
        // Never trust the client-supplied idClient/organizationId — always the token's.
        request.setIdClient(principal.clientId());
        request.setOrganizationId(principal.organizationId());
        request.setEmailClient(principal.email());

        if (request.getNomClient() != null) {
            return quotationProposalService.create(request);
        }
        // The portal JWT only carries an email, not a display name — the
        // frontend normally sends its own nomClient (fetched via
        // getMyClientInfo), but fall back to a real lookup rather than
        // showing the client's raw email address as their "name".
        return clientUseCase.getAllClients()
                .filter(c -> principal.clientId().equals(c.getIdClient()))
                .next()
                .map(c -> c.getRaisonSociale() != null ? c.getRaisonSociale() : c.getUsername())
                .defaultIfEmpty(principal.email())
                .flatMap(name -> {
                    request.setNomClient(name);
                    return quotationProposalService.create(request);
                });
    }

    @GetMapping("/api/portal/quotation-proposals")
    public Flux<QuotationProposalResponse> getMyQuotationProposals(@RequestHeader("Authorization") String authHeader) {
        var principal = authenticate(authHeader);
        requireRole(principal, "CUSTOMER");
        return quotationProposalService.getByClientAndOrganization(principal.clientId(), principal.organizationId());
    }

    @GetMapping("/api/portal/me/client")
    public Mono<ClientResponse> getMyClientInfo(@RequestHeader("Authorization") String authHeader) {
        var principal = authenticate(authHeader);
        requireRole(principal, "CUSTOMER");
        // getClientById's single-actor Kernel lookup is currently broken for
        // some clients (unrelated pre-existing bug) — getAllClients works
        // reliably, so find our own record from that list instead.
        return clientUseCase.getAllClients()
                .filter(c -> principal.clientId().equals(c.getIdClient()))
                .next()
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Client record not found")));
    }

    @GetMapping("/api/portal/organization")
    public Mono<Map> getMyOrganizationBranding(@RequestHeader("Authorization") String authHeader) {
        var principal = authenticate(authHeader);
        return Mono.zip(
                salesCoreWebClient.get()
                        .uri("/api/organizations/{id}/branding", principal.organizationId())
                        .retrieve()
                        .bodyToMono(Map.class),
                settingService.getOrganizationSettings(principal.organizationId())
        ).map(tuple -> {
            // The sales-core branding snapshot's logo is stale (set once at
            // seller-creation) — override with whatever's actually configured
            // on the Settings page, same as the seller-login flow does.
            Map<String, Object> branding = new java.util.HashMap<>(tuple.getT1());
            String configuredLogo = tuple.getT2().getUri();
            if (configuredLogo != null && !configuredLogo.isBlank()) {
                branding.put("organizationLogoUri", configuredLogo);
            }
            return branding;
        });
    }

    private void requireOwnership(PortalTokenVerifier.PortalPrincipal principal, String documentPartyId, UUID documentOrgId) {
        if (documentPartyId == null || !principal.clientId().toString().equalsIgnoreCase(documentPartyId)
                || !principal.organizationId().equals(documentOrgId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This document doesn't belong to your account");
        }
    }
}
