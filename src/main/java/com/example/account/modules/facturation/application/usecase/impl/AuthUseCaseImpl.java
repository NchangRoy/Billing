package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.facturation.domain.port.input.AuthUseCase;
import com.example.account.modules.facturation.domain.port.output.AuthServicePort;
import com.example.account.modules.facturation.dto.request.CreateSellerRequest;
import com.example.account.modules.facturation.dto.request.LoginRequest;
import com.example.account.modules.facturation.dto.request.MfaConfirmRequest;
import com.example.account.modules.facturation.dto.request.PinLoginRequest;
import com.example.account.modules.facturation.dto.request.SellerUIPermissionsRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerUIPermissionsResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerListItemResponse;
import com.example.account.modules.facturation.model.entity.SellerAuthContext;
import com.example.account.modules.facturation.model.enums.SaleSize;
import com.example.account.modules.facturation.model.enums.SellerPermission;
import com.example.account.modules.facturation.model.enums.SellerRole;
import com.example.account.modules.facturation.repository.SellerAgencyAssignmentRepository;
import com.example.account.modules.facturation.repository.SellerAuthContextRepository;
import com.example.account.modules.facturation.service.SellerLocalService;
import com.example.account.modules.facturation.service.SellerUIPermissionsLocalService;
import com.example.account.modules.facturation.service.SubscriptionService;
import com.example.account.modules.settings.service.SettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUseCaseImpl implements AuthUseCase {

    private final AuthServicePort authServicePort;
    private final SellerAuthContextRepository sellerAuthContextRepository;
    private final SettingService settingService;
    private final SellerUIPermissionsLocalService sellerUIPermissionsLocalService;
    private final SubscriptionService subscriptionService;
    private final SellerLocalService sellerLocalService;
    private final SellerAgencyAssignmentRepository sellerAgencyAssignmentRepository;

    @Override
    public Mono<SellerAuthResponse> login(LoginRequest request) {
        return authServicePort.login(request.getUsername(), request.getPassword(), request.getTenantId(), request.getOrganizationId())
                .flatMap(this::attachExistingSeller)
                .flatMap(this::cacheKernelSession)
                .flatMap(this::withOrganizationSettings);
    }

    @Override
    public Mono<SellerAuthResponse> tryOut(LoginRequest request) {
        return authServicePort.tryOut(request.getUsername(), request.getPassword(), request.getTenantId(), request.getOrganizationId())
                .flatMap(response -> ensureSellerProvisioned(response, request.getPlanType()))
                .flatMap(this::cacheKernelSession)
                .flatMap(this::withOrganizationSettings);
    }

    /**
     * Second step once login()/tryOut() came back with mfaRequired=true.
     * request.tryOut() picks up the same branching login()/tryOut() use —
     * attach an existing seller for Sign In, auto-provision an OWNER seller
     * for Try Out — since Kernel's mfaToken itself carries no memory of which
     * entry point the account originally used.
     */
    @Override
    public Mono<SellerAuthResponse> confirmMfa(MfaConfirmRequest request) {
        Mono<SellerAuthResponse> resolved = authServicePort.confirmMfa(
                request.getMfaToken(), request.getCode(), request.getOrganizationId());
        return (request.isTryOut()
                // Plan selection (like org/tenant selection) isn't wired up on the
                // MFA continuation path in the frontend yet — null planType means
                // requiresPlanSelection can still come back, just not be resolved
                // through this entry point.
                ? resolved.flatMap(response -> ensureSellerProvisioned(response, null))
                : resolved.flatMap(this::attachExistingSeller))
                .flatMap(this::cacheKernelSession)
                .flatMap(this::withOrganizationSettings);
    }

    /**
     * Caches the Kernel refreshToken this login/tryOut/confirmMfa call just
     * received, keyed by the seller resolved above, so a later PIN login (see
     * loginByLocalPin) can mint a fresh accessToken instead of having no
     * Kernel session at all. Only sellers with an existing local PIN context
     * get one cached — a response with no resolved seller id (e.g. the
     * requiresOrganizationSelection branch) or no refreshToken (Kernel didn't
     * issue one) is left as-is; best-effort, never fails the login itself.
     */
    private Mono<SellerAuthResponse> cacheKernelSession(SellerAuthResponse response) {
        if (response.getId() == null || response.getRefreshToken() == null) {
            return Mono.just(response);
        }
        return sellerAuthContextRepository.findBySellerId(response.getId())
                .flatMap(context -> {
                    context.setRefreshToken(response.getRefreshToken());
                    context.setRefreshExpiresAt(parseIsoDateTime(response.getRefreshExpiresAt()));
                    context.setUpdatedAt(java.time.LocalDateTime.now());
                    return sellerAuthContextRepository.save(context);
                })
                .thenReturn(response)
                .onErrorReturn(response);
    }

    private java.time.LocalDateTime parseIsoDateTime(String isoDateTime) {
        if (isoDateTime == null) {
            return null;
        }
        try {
            return java.time.OffsetDateTime.parse(isoDateTime).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Sign In only ever authenticated a person who's already a known seller —
     * if one now exists for this Kernel account (e.g. provisioned by an
     * earlier Try Out, or invited normally), reflect its real role and
     * permissions instead of leaving them null. Doesn't create one: an
     * account with no seller record just gets Kernel's bare login data,
     * same as before.
     */
    private Mono<SellerAuthResponse> attachExistingSeller(SellerAuthResponse response) {
        return findExistingSeller(response)
                .flatMap(existing -> applyExistingSeller(response, existing))
                .switchIfEmpty(Mono.defer(() -> applyOwnerBypassIfNeeded(response)));
    }

    /**
     * No remote sales-core seller record matched (e.g. the organization's own
     * owner, who was never "invited" as a seller) — role may still have come
     * from AuthServiceAdapter's local-seller enrichment. The owner's access
     * can't depend on either of those existing, so grant it here too.
     */
    private Mono<SellerAuthResponse> applyOwnerBypassIfNeeded(SellerAuthResponse response) {
        if (response.getRole() == SellerRole.OWNER) {
            response.setUiPermissions(sellerUIPermissionsLocalService.fullAccess(response.getId()));
        }
        return Mono.just(response);
    }

    /**
     * Try Out authenticates against Kernel directly, so there's no local seller
     * record backing the session yet. On first use for a given (org, username),
     * auto-provision one as OWNER with every permission granted — this account
     * already owns the Kernel organization, so it should walk in with full
     * access rather than the empty-permission default a brand-new seller gets.
     * An existing seller (from a prior Try Out or a normal invite) is left as-is
     * and just mapped onto the response, so this never silently re-grants access
     * an admin deliberately restricted later.
     * <p>
     * Before that first provisioning, the organization needs a plan: if none
     * exists yet, this pauses (requiresPlanSelection=true, same pattern as
     * requiresOrganizationSelection) instead of provisioning, so the frontend
     * can show a plan picker and resubmit with planType set.
     */
    private Mono<SellerAuthResponse> ensureSellerProvisioned(SellerAuthResponse response, String planType) {
        if (Boolean.TRUE.equals(response.getRequiresOrganizationSelection()) || response.getOrganizationId() == null) {
            return Mono.just(response);
        }
        return findExistingSeller(response)
                .flatMap(existing -> applyExistingSeller(response, existing))
                .switchIfEmpty(Mono.defer(() -> resolvePlanAndProvision(response, planType)));
    }

    private Mono<SellerAuthResponse> resolvePlanAndProvision(SellerAuthResponse response, String planType) {
        return subscriptionService.exists(response.getOrganizationId())
                .flatMap(hasSubscription -> {
                    if (hasSubscription) {
                        return provisionOwnerSeller(response);
                    }
                    if (planType == null) {
                        response.setRequiresPlanSelection(true);
                        return Mono.just(response);
                    }
                    return subscriptionService.create(response.getOrganizationId(), planType)
                            .then(provisionOwnerSeller(response));
                });
    }

    /**
     * AuthServiceAdapter.enrichWithLocalSeller already ran a lenient match
     * against both Kernel's login email and its recovery email and, on a hit,
     * overwrote response.getId() with that local seller's real id — Kernel's
     * own login/account email and the email a seller was invited under can
     * genuinely differ (seen live: a login of "nchang@yowyob.com" whose
     * seller record is under "nchangfruroy@gmail.com"), so repeating a
     * single-email match against response.getEmail() here would silently
     * miss it and leave id/uiPermissions unset for the rest of the session.
     * Falls back to that single-email match only if the adapter found nothing
     * (response.getId() is then still Kernel's own raw account id, which
     * won't match any local seller — the org filter below is just defensive
     * in case it coincidentally did).
     */
    private Mono<SellerListItemResponse> findExistingSeller(SellerAuthResponse response) {
        if (Boolean.TRUE.equals(response.getRequiresOrganizationSelection()) || response.getOrganizationId() == null) {
            return Mono.empty();
        }
        return lookupById(response.getOrganizationId(), response.getId())
                .switchIfEmpty(Mono.defer(() ->
                        sellerLocalService.findByEmail(response.getOrganizationId(), response.getEmail())
                                .flatMap(this::withAgency)));
    }

    private Mono<SellerListItemResponse> lookupById(java.util.UUID organizationId, java.util.UUID sellerId) {
        if (sellerId == null) {
            return Mono.empty();
        }
        return sellerLocalService.findById(sellerId)
                .filter(entity -> organizationId.equals(entity.getOrganizationId()))
                .flatMap(this::withAgency);
    }

    private Mono<SellerListItemResponse> withAgency(
            com.example.account.modules.facturation.adapter.output.persistence.SellerPersistenceEntity entity) {
        return sellerAgencyAssignmentRepository.findBySellerId(entity.getId())
                .map(assignment -> toSellerListItemResponse(entity, assignment))
                .defaultIfEmpty(toSellerListItemResponse(entity, null));
    }

    private SellerListItemResponse toSellerListItemResponse(
            com.example.account.modules.facturation.adapter.output.persistence.SellerPersistenceEntity e,
            com.example.account.modules.facturation.model.entity.SellerAgencyAssignment assignment) {
        SellerListItemResponse r = new SellerListItemResponse();
        r.setId(e.getId());
        r.setEmail(e.getEmail());
        r.setFirstName(e.getFirstName());
        r.setLastName(e.getLastName());
        r.setRole(e.getRole());
        r.setSalePoint(e.getSalePoint());
        r.setProfileImageUrl(e.getProfileImageUrl());
        r.setPermissions(e.getPermissions() == null ? List.of() : java.util.Arrays.stream(e.getPermissions())
                .map(SellerPermission::valueOf).toList());
        r.setPermittedSaleSizes(e.getPermittedSaleSizes() == null ? List.of() : java.util.Arrays.stream(e.getPermittedSaleSizes())
                .map(SaleSize::valueOf).toList());
        r.setOrganizationId(e.getOrganizationId());
        r.setSalesPointId(e.getSalesPointId());
        if (assignment != null) {
            r.setAgencyId(assignment.getAgencyId());
            r.setAgency(assignment.getAgencyName());
        }
        return r;
    }

    private Mono<SellerAuthResponse> applyExistingSeller(SellerAuthResponse response, SellerListItemResponse seller) {
        response.setId(seller.getId());
        response.setRole(seller.getRole() != null ? SellerRole.valueOf(seller.getRole()) : null);
        response.setAgency(seller.getAgency());
        response.setSalePoint(seller.getSalePoint());
        response.setPermissions(seller.getPermissions());
        response.setPermittedSaleSizes(seller.getPermittedSaleSizes());
        response.setAgencyId(seller.getAgencyId());
        response.setSalesPointId(seller.getSalesPointId());
        response.setMustChangePassword(seller.getMustChangePassword());
        // The owner must always see and reach everything in their own organization —
        // this can't depend on a configured ui_permissions row (there may never be one).
        if (response.getRole() == SellerRole.OWNER) {
            response.setUiPermissions(sellerUIPermissionsLocalService.fullAccess(seller.getId()));
            return Mono.just(response);
        }
        return sellerUIPermissionsLocalService.get(seller.getId())
                .doOnNext(response::setUiPermissions)
                .thenReturn(response)
                .onErrorReturn(response);
    }

    private Mono<SellerAuthResponse> provisionOwnerSeller(SellerAuthResponse response) {
        log.info("Try Out: auto-provisioning OWNER seller '{}' for organization {}",
                response.getUsername(), response.getOrganizationId());

        List<String> permissions = List.of(SellerPermission.values()).stream().map(Enum::name).toList();
        List<String> permittedSaleSizes = List.of(SaleSize.values()).stream().map(Enum::name).toList();

        // Kernel's login response has no first/last name split — username is
        // the only identity Try Out actually has available at this point.
        return sellerLocalService.create(
                        response.getOrganizationId(), response.getEmail(), response.getUsername(), "Owner",
                        SellerRole.OWNER.name(), permissions, permittedSaleSizes, null, null, null)
                .flatMap(created -> {
                    response.setId(created.getId());
                    response.setRole(SellerRole.OWNER);
                    response.setPermissions(List.of(SellerPermission.values()));
                    response.setPermittedSaleSizes(List.of(SaleSize.values()));
                    return sellerUIPermissionsLocalService.set(created.getId(), response.getOrganizationId(), null, fullUiPermissions())
                            .doOnNext(response::setUiPermissions)
                            .thenReturn(response)
                            .onErrorReturn(response);
                });
    }

    private SellerUIPermissionsRequest fullUiPermissions() {
        SellerUIPermissionsRequest p = new SellerUIPermissionsRequest();
        p.setSectionSalesManagement(true);
        p.setSalesQuotations(true);
        p.setSalesQuotationProposals(true);
        p.setSalesProformaInvoice(true);
        p.setSalesSalesOrders(true);
        p.setSalesInvoices(true);
        p.setSalesDeliveryNote(true);
        p.setSalesCreditNotes(true);
        p.setSalesBackOrders(true);
        p.setSectionPurchasingLogistics(true);
        p.setPurchasingPurchaseOrder(true);
        p.setPurchasingGoodsReceiptNote(true);
        p.setPurchasingSupplierInvoice(true);
        p.setSectionAccountingJournals(true);
        p.setJournalsQuotation(true);
        p.setJournalsSaleOrder(true);
        p.setJournalsPurchaseOrder(true);
        p.setJournalsClientInvoice(true);
        p.setJournalsSupplierInvoice(true);
        p.setSectionOrganization(true);
        p.setOrganizationAgencies(true);
        p.setOrganizationSellers(true);
        p.setOrganizationCustomers(true);
        p.setOrganizationSuppliers(true);
        p.setOrganizationSalePoints(true);
        p.setOrganizationSessions(true);
        p.setOrganizationProducts(true);
        p.setSectionSettings(true);
        p.setSettingsPreferences(true);
        p.setSectionProductSettings(true);
        p.setProductSettingsUom(true);
        p.setProductSettingsCategories(true);
        p.setProductSettingsProducts(true);
        return p;
    }

    /**
     * Identifies a seller from Billing's own local PIN store (SellerAuthContext)
     * instead of sales-core's PIN login. Fetches the matching seller by listing
     * the org's sellers and filtering client-side — a stand-in for a direct
     * get-by-id call until sales-core's GET /api/sellers/{sellerId} (added
     * locally, not yet deployed) is reachable from here.
     *
     * PIN login never talks to Kernel with credentials of its own, so without
     * this it would hand the frontend a SellerAuthResponse with no
     * accessToken — every subsequent Kernel-bound request would then go out
     * with no caller identity at all. Mints a fresh accessToken from the
     * refreshToken cached the last time this seller logged in normally (see
     * cacheKernelSession); a seller who never has, or whose cached refresh
     * token has since expired, gets a clear 401 instead.
     */
    @Override
    public Mono<SellerAuthResponse> loginByLocalPin(PinLoginRequest request) {
        return sellerAuthContextRepository.findByOrganizationIdAndPin(request.getOrganizationId(), request.getPin())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid PIN")))
                .flatMap(context -> {
                    if (context.getRefreshToken() == null) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                "PIN login requires signing in normally at least once first."));
                    }
                    return sellerLocalService.findById(context.getSellerId())
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "Seller not found: " + context.getSellerId())))
                            .map(this::mapToSellerAuthResponse)
                            .flatMap(response -> attachRefreshedKernelSession(response, context));
                })
                .flatMap(this::withOrganizationSettings);
    }

    /**
     * Refresh tokens are typically single-use (Kernel rotates them on every
     * refresh), so the rotated one Kernel hands back here is persisted
     * immediately — reusing the old one on the next PIN login would fail.
     */
    private Mono<SellerAuthResponse> attachRefreshedKernelSession(SellerAuthResponse response, SellerAuthContext context) {
        return authServicePort.refreshAccessToken(context.getRefreshToken())
                .flatMap(refreshed -> {
                    response.setAccessToken(refreshed.getAccessToken());
                    context.setRefreshToken(refreshed.getRefreshToken());
                    context.setRefreshExpiresAt(parseIsoDateTime(refreshed.getRefreshExpiresAt()));
                    context.setUpdatedAt(java.time.LocalDateTime.now());
                    return sellerAuthContextRepository.save(context).thenReturn(response);
                });
    }

    private SellerAuthResponse mapToSellerAuthResponse(
            com.example.account.modules.facturation.adapter.output.persistence.SellerPersistenceEntity seller) {
        SellerAuthResponse r = new SellerAuthResponse();
        r.setId(seller.getId());
        r.setRole(seller.getRole() != null ? SellerRole.valueOf(seller.getRole()) : null);
        r.setSalePoint(seller.getSalePoint());
        r.setPermissions(seller.getPermissions() == null ? List.of() : java.util.Arrays.stream(seller.getPermissions())
                .map(SellerPermission::valueOf).toList());
        r.setPermittedSaleSizes(seller.getPermittedSaleSizes() == null ? List.of() : java.util.Arrays.stream(seller.getPermittedSaleSizes())
                .map(SaleSize::valueOf).toList());
        r.setOrganizationId(seller.getOrganizationId());
        r.setSalesPointId(seller.getSalesPointId());
        r.setCreatedAt(seller.getCreatedAt() == null ? null : seller.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant());
        return r;
    }

    private Mono<SellerAuthResponse> withOrganizationSettings(SellerAuthResponse response) {
        if (response.getOrganizationId() == null) {
            return Mono.just(response);
        }
        return Mono.zip(
                        settingService.getOrganizationSettings(response.getOrganizationId()),
                        settingService.listSequenceSettings(response.getOrganizationId()).collectList()
                )
                .map(tuple -> {
                    response.setOrganizationSettings(tuple.getT1());
                    response.setDocumentNumberingSettings(tuple.getT2());
                    // organizationLogoUri is a snapshot taken at seller-creation
                    // time and never updated — every document preview reads
                    // this field, so keep it in sync with whatever's actually
                    // configured on the Settings page (organizationSettings.uri
                    // is fetched fresh above, on every login).
                    String configuredLogo = tuple.getT1().getUri();
                    if (configuredLogo != null && !configuredLogo.isBlank()) {
                        response.setOrganizationLogoUri(configuredLogo);
                    }
                    return response;
                })
                .onErrorReturn(response);
    }
}
