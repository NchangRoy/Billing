package com.example.account.modules.facturation.application.usecase.impl;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.core.domain.port.output.AgencyServicePort;
import com.example.account.modules.facturation.domain.port.input.SellerAdminUseCase;
import com.example.account.modules.facturation.dto.request.AssignAgencyRequest;
import com.example.account.modules.facturation.dto.request.CreateSellerRequest;
import com.example.account.modules.facturation.dto.request.SellerUIPermissionsRequest;
import com.example.account.modules.facturation.dto.request.UpdateSellerPermissionsRequest;
import com.example.account.modules.facturation.dto.request.UpdateSellerPhotoRequest;
import com.example.account.modules.facturation.dto.request.UpdateSellerRoleRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.AssignAgencyResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.CreateSellerResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.OrganizationEmployeeResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerListItemResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerUIPermissionsResponse;
import com.example.account.modules.facturation.adapter.output.persistence.SellerPersistenceEntity;
import com.example.account.modules.facturation.model.entity.SellerAgencyAssignment;
import com.example.account.modules.facturation.model.entity.SellerAuthContext;
import com.example.account.modules.facturation.repository.SellerAgencyAssignmentRepository;
import com.example.account.modules.facturation.repository.SellerAuthContextRepository;
import com.example.account.modules.notification.dto.SendSellerInvitationEmailRequest;
import com.example.account.modules.notification.service.SellerEmailService;
import com.example.account.modules.facturation.service.SellerLocalService;
import com.example.account.modules.facturation.service.SellerUIPermissionsLocalService;
import com.example.account.modules.shared.dto.kernel.KernelAgencyResponse;
import com.example.account.modules.shared.dto.kernel.KernelApiResponse;
import com.example.account.modules.shared.dto.kernel.KernelEmployeeMembershipResponse;
import com.example.account.modules.shared.dto.kernel.KernelInviteEmployeeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerAdminUseCaseImpl implements SellerAdminUseCase {

    private static final SecureRandom PIN_RANDOM = new SecureRandom();

    private final R2dbcEntityTemplate entityTemplate;
    private final SellerEmailService sellerEmailService;
    private final AgencyServicePort agencyServicePort;
    private final SellerAgencyAssignmentRepository sellerAgencyAssignmentRepository;
    private final SellerAuthContextRepository sellerAuthContextRepository;
    private final SellerUIPermissionsLocalService sellerUIPermissionsLocalService;
    private final SellerLocalService sellerLocalService;

    @Qualifier("kernelWebClient")
    private final WebClient kernelWebClient;

    private static final ParameterizedTypeReference<KernelApiResponse<List<KernelEmployeeMembershipResponse>>> KERNEL_EMPLOYEE_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    /**
     * Profile (role, permissions, sale point) now comes from Billing's own
     * sellers table (SellerLocalService) rather than Kernel's sales-core —
     * agency and PIN/status are merged in the same way as before, from their
     * own already-local tables.
     */
    @Override
    public Flux<SellerListItemResponse> listSellers(UUID organizationId) {
        return sellerLocalService.list(organizationId)
                .map(this::toSellerListItemResponse)
                .collectList()
                .flatMapMany(sellers -> {
                    if (sellers.isEmpty()) {
                        return Flux.empty();
                    }
                    List<UUID> sellerIds = sellers.stream().map(SellerListItemResponse::getId).toList();
                    return Mono.zip(
                                    sellerAgencyAssignmentRepository.findBySellerIdIn(sellerIds)
                                            .collectMap(SellerAgencyAssignment::getSellerId, Function.identity()),
                                    sellerAuthContextRepository.findBySellerIdIn(sellerIds)
                                            .collectMap(SellerAuthContext::getSellerId, Function.identity()))
                            .flatMapMany(tuple -> Flux.fromIterable(sellers)
                                    .doOnNext(seller -> {
                                        applyAssignment(seller, tuple.getT1().get(seller.getId()));
                                        applyAuthContext(seller, tuple.getT2().get(seller.getId()));
                                    }));
                });
    }

    private SellerListItemResponse toSellerListItemResponse(SellerPersistenceEntity e) {
        SellerListItemResponse r = new SellerListItemResponse();
        r.setId(e.getId());
        r.setEmail(e.getEmail());
        r.setFirstName(e.getFirstName());
        r.setLastName(e.getLastName());
        r.setRole(e.getRole());
        r.setSalePoint(e.getSalePoint());
        r.setProfileImageUrl(e.getProfileImageUrl());
        r.setPermissions(e.getPermissions() == null ? List.of() : java.util.Arrays.stream(e.getPermissions())
                .map(com.example.account.modules.facturation.model.enums.SellerPermission::valueOf).toList());
        r.setPermittedSaleSizes(e.getPermittedSaleSizes() == null ? List.of() : java.util.Arrays.stream(e.getPermittedSaleSizes())
                .map(com.example.account.modules.facturation.model.enums.SaleSize::valueOf).toList());
        r.setOrganizationId(e.getOrganizationId());
        r.setSalesPointId(e.getSalesPointId());
        r.setCreatedAt(e.getCreatedAt() == null ? null : e.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant());
        return r;
    }

    private void applyAssignment(SellerListItemResponse seller, SellerAgencyAssignment assignment) {
        if (assignment == null) {
            return;
        }
        seller.setAgencyId(assignment.getAgencyId());
        seller.setAgency(assignment.getAgencyName());
    }

    private void applyAuthContext(SellerListItemResponse seller, SellerAuthContext authContext) {
        if (authContext == null) {
            seller.setStatus("ACTIVE");
            return;
        }
        seller.setPin(authContext.getPin());
        seller.setStatus(authContext.getStatus() != null ? authContext.getStatus() : "ACTIVE");
    }

    /**
     * "Invite" flow. Two cases, distinguished by whether this email is already
     * a Kernel employee of the org:
     * <ul>
     *   <li>Not yet an employee — invited into the org via Kernel's own
     *   employee-invite (so they get a real Kernel account/accept-link), local
     *   seller marked PENDING until they're a known employee again (listSellers
     *   will keep showing PENDING — nothing currently flips it to ACTIVE
     *   automatically since Billing has no webhook for Kernel invite-acceptance).
     *   The Kernel invite is best-effort: if it still fails (e.g. a race with
     *   another invite), the seller is still created so this never blocks on a
     *   step the admin can't see or retry from here.</li>
     *   <li>Already an employee — Kernel's employee-invite isn't for this (it's
     *   for bringing a new person into the org, not someone already in it; calling
     *   it anyway just gets a 409 Conflict). Skip it entirely and mark the seller
     *   ACTIVE immediately, same as {@link #createSellerFromEmployee}.</li>
     * </ul>
     * Either way, a credentials email (our own template, with their PIN) is sent —
     * this is still the entry point for "give this person Billing login access",
     * unlike createSellerFromEmployee which assumes that's already been handled.
     * The seller profile itself is purely local now (SellerLocalService) —
     * Kernel's sales-core is never involved in creating it.
     */
    @Override
    public Mono<CreateSellerResponse> createSeller(CreateSellerRequest request) {
        fillDerivedFieldsFromEmail(request);
        return isExistingOrgEmployee(request)
                .flatMap(alreadyEmployee -> (alreadyEmployee ? Mono.<Void>empty() : inviteKernelEmployee(request))
                        .then(createLocalSeller(request))
                        .flatMap(response -> {
                            String pin = generatePin();
                            response.setPin(pin);
                            String status = alreadyEmployee ? "ACTIVE" : "PENDING";
                            return saveAuthContext(response.getId(), request.getOrganizationId(), pin, status)
                                    .then(sendInvitation(request, response, pin))
                                    .thenReturn(response);
                        }));
    }

    /**
     * "Add from employee" flow: the person is already a known Kernel org
     * employee (picked from listOrganizationEmployees), so no Kernel invite is
     * needed — they already have working Kernel credentials. Just creates the
     * local seller record and marks it ACTIVE immediately. No credentials
     * email is sent since nothing new was provisioned for them to log in with.
     */
    @Override
    public Mono<CreateSellerResponse> createSellerFromEmployee(CreateSellerRequest request) {
        fillDerivedFieldsFromEmail(request);
        return createLocalSeller(request)
                .flatMap(response -> {
                    String pin = generatePin();
                    response.setPin(pin);
                    return saveAuthContext(response.getId(), request.getOrganizationId(), pin, "ACTIVE")
                            .thenReturn(response);
                });
    }

    private Mono<CreateSellerResponse> createLocalSeller(CreateSellerRequest request) {
        String role = request.getRole() == null || request.getRole().isBlank() ? "SELLER" : request.getRole();
        return sellerLocalService.create(
                        request.getOrganizationId(), request.getEmail(), request.getFirstName(), request.getLastName(),
                        role, request.getPermissions(), request.getPermittedSaleSizes(),
                        request.getSalePoint(), request.getSalesPointId(), request.getSalesPointAddress())
                .map(entity -> {
                    CreateSellerResponse response = new CreateSellerResponse();
                    response.setId(entity.getId());
                    response.setFirstName(entity.getFirstName());
                    response.setLastName(entity.getLastName());
                    response.setRole(entity.getRole());
                    return response;
                });
    }

    /**
     * organizationId query param is required by Kernel even though the caller's
     * own token already scopes it (matches the pattern already used for
     * agencies and employee-roster lookups elsewhere in this class). Best-effort:
     * failures are logged and swallowed so a missing/duplicate Kernel account
     * never blocks local seller creation. Username here is a one-off slug for
     * this Kernel call only — Billing's own seller data has no such field.
     */
    private Mono<Void> inviteKernelEmployee(CreateSellerRequest request) {
        KernelInviteEmployeeRequest invite = new KernelInviteEmployeeRequest();
        invite.setEmail(request.getEmail());
        invite.setFirstName(request.getFirstName());
        invite.setLastName(request.getLastName());
        invite.setUsername(deriveEmailSlug(request.getEmail()));
        return kernelWebClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/employees/invite")
                        .queryParam("organizationId", request.getOrganizationId())
                        .build())
                .bodyValue(invite)
                .retrieve()
                .bodyToMono(String.class)
                .then()
                .onErrorResume(e -> {
                    log.warn("Kernel employee invite failed for {} (continuing with local seller creation anyway): {}",
                            request.getEmail(), e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Source for the "add from employee" picker: this org's Kernel employee
     * roster, flagged with whether each person already has a seller record
     * here (cross-referenced by email, since seller and employee records
     * don't share a common id).
     */
    @Override
    public Flux<OrganizationEmployeeResponse> listOrganizationEmployees(UUID organizationId) {
        return sellerLocalService.list(organizationId)
                .map(seller -> seller.getEmail() == null ? "" : seller.getEmail().trim().toLowerCase())
                .collect(java.util.stream.Collectors.toSet())
                .flatMapMany(sellerEmails -> fetchOrgEmployees(organizationId)
                        .map(employee -> toOrganizationEmployeeResponse(employee, sellerEmails)));
    }

    private OrganizationEmployeeResponse toOrganizationEmployeeResponse(KernelEmployeeMembershipResponse employee, java.util.Set<String> sellerEmails) {
        OrganizationEmployeeResponse response = new OrganizationEmployeeResponse();
        response.setId(employee.getId());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setStatus(employee.getStatus());
        boolean alreadySeller = employee.getEmail() != null
                && sellerEmails.contains(employee.getEmail().trim().toLowerCase());
        response.setAlreadySeller(alreadySeller);
        return response;
    }

    private Flux<KernelEmployeeMembershipResponse> fetchOrgEmployees(UUID organizationId) {
        return kernelWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/employees")
                        .queryParam("organizationId", organizationId)
                        .build())
                .retrieve()
                .bodyToMono(KERNEL_EMPLOYEE_LIST_TYPE)
                .map(KernelApiResponse::getData)
                .flatMapMany(Flux::fromIterable);
    }

    /**
     * Distinguishes createSeller's two cases (see its javadoc): an email
     * already on this org's Kernel employee roster doesn't need — and can't
     * use, Kernel rejects it with 409 — the new-employee invite. Best-effort
     * like the invite call itself: a failed roster lookup falls back to
     * "not an employee" so this never blocks seller creation on Kernel being
     * unreachable, just costs that one invite a redundant (but harmless)
     * Kernel call.
     */
    private Mono<Boolean> isExistingOrgEmployee(CreateSellerRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        if (email.isEmpty()) {
            return Mono.just(false);
        }
        return fetchOrgEmployees(request.getOrganizationId())
                .any(employee -> employee.getEmail() != null && employee.getEmail().trim().equalsIgnoreCase(email))
                .onErrorReturn(false);
    }

    private Mono<SellerAuthContext> saveAuthContext(UUID sellerId, UUID organizationId, String pin, String status) {
        LocalDateTime now = LocalDateTime.now();
        SellerAuthContext context = SellerAuthContext.builder()
                .id(UUID.randomUUID())
                .sellerId(sellerId)
                .organizationId(organizationId)
                .pin(pin)
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return entityTemplate.insert(context);
    }

    private Mono<Void> sendInvitation(CreateSellerRequest request, CreateSellerResponse response, String pin) {
        return resolveRealInvitationEmail(request)
                .flatMap(realEmail -> {
                    SendSellerInvitationEmailRequest invitation = new SendSellerInvitationEmailRequest();
                    invitation.setOrganizationId(request.getOrganizationId());
                    invitation.setEmail(realEmail);
                    invitation.setFirstName(response.getFirstName());
                    invitation.setRole(response.getRole());
                    invitation.setAgency(request.getAgency());
                    invitation.setPin(pin);
                    return sellerEmailService.sendInvitation(invitation);
                })
                .onErrorResume(e -> {
                    log.error("Failed to send seller invitation email to {}: {}", request.getEmail(), e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * request.getEmail() is whatever login identity the caller typed (e.g.
     * Kernel's own login alias) — it's what has to match on future normal
     * logins for the PIN flow to link up, but it's often not a real inbox the
     * person actually checks. Kernel's own organization-employee roster
     * carries their real profile email separately (seen live: an employee
     * invited/logged in as "nchang@yowyob.com" has employee-record email
     * "nchangfruroy@gmail.com"). Matches by name since the seller/employee
     * records don't share a common id; falls back to request.getEmail() if
     * no employee record matches, so this never blocks seller creation.
     */
    private Mono<String> resolveRealInvitationEmail(CreateSellerRequest request) {
        if (request.getFirstName() == null && request.getLastName() == null) {
            return Mono.just(request.getEmail());
        }
        return fetchOrgEmployees(request.getOrganizationId())
                .filter(employee -> namesMatch(request, employee))
                .next()
                .map(KernelEmployeeMembershipResponse::getEmail)
                .switchIfEmpty(Mono.just(request.getEmail()))
                .onErrorResume(e -> {
                    log.warn("Failed to resolve real employee email for {} {}, falling back to {}: {}",
                            request.getFirstName(), request.getLastName(), request.getEmail(), e.getMessage());
                    return Mono.just(request.getEmail());
                });
    }

    private boolean namesMatch(CreateSellerRequest request, KernelEmployeeMembershipResponse employee) {
        return equalsIgnoreCaseTrim(request.getFirstName(), employee.getFirstName())
                && equalsIgnoreCaseTrim(request.getLastName(), employee.getLastName());
    }

    private boolean equalsIgnoreCaseTrim(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }

    private String generatePin() {
        return String.format("%05d", PIN_RANDOM.nextInt(100_000));
    }

    /**
     * Invite flow only collects email + role now — firstName/lastName are
     * derived from the email's local part as a placeholder until the person's
     * real name comes back once they accept the Kernel invite and fill in
     * their profile.
     */
    private void fillDerivedFieldsFromEmail(CreateSellerRequest request) {
        String slug = deriveEmailSlug(request.getEmail());
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            request.setFirstName(slug.isEmpty() ? "Seller" : slug.substring(0, 1).toUpperCase() + slug.substring(1));
        }
        if (request.getLastName() == null || request.getLastName().isBlank()) {
            // Kernel's /api/employees/invite rejects a blank lastName outright
            // ("lastName is required", ORGANIZATION_INVALID_REQUEST) — same
            // placeholder-until-they-fill-in-their-real-profile reasoning as
            // firstName above, just needs to be non-blank.
            request.setLastName("Seller");
        }
    }

    private String deriveEmailSlug(String email) {
        String localPart = email == null ? "" : email.split("@")[0];
        return localPart.replaceAll("[^A-Za-z0-9._-]", "");
    }

    /**
     * Handled entirely in Billing rather than forwarded to sales-core's own
     * /api/sellers/{id}/agency: that endpoint needs an internal Kernel
     * round-trip (sales-core's own service-account login) that 401s whenever
     * KERNEL_BASE_URL isn't set in that deployment's environment — a config
     * issue on Kernel's side, not fixable from here. Billing already has its
     * own working Kernel connection (AgencyServicePort) to validate the
     * agency, so the assignment is validated and stored locally instead.
     *
     * organizationId comes from the request context instead of the seller
     * record itself, the same X-Organization-ID header every other
     * seller-admin call already relies on.
     */
    @Override
    public Mono<AssignAgencyResponse> assignAgency(UUID sellerId, AssignAgencyRequest request) {
        return ReactiveOrganizationContext.getOrganizationId()
                .flatMap(organizationId -> agencyServicePort.findById(organizationId, request.getAgencyId())
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Agency not found in this organization: " + request.getAgencyId())))
                        .flatMap(agency -> upsertAssignment(sellerId, organizationId, agency)));
    }

    /**
     * Spring Data R2DBC's save() decides insert-vs-update purely by whether the
     * @Id field is already set — this entity doesn't implement Persistable, so
     * a freshly-built row with a client-generated id would be misread as an
     * "existing" row to update and fail with "does not exist". Same reasoning
     * as saveAuthContext() above: use entityTemplate.insert() explicitly for
     * the genuinely-new case, save() only for a row that's confirmed to exist.
     */
    private Mono<AssignAgencyResponse> upsertAssignment(UUID sellerId, UUID organizationId, KernelAgencyResponse agency) {
        LocalDateTime now = LocalDateTime.now();
        return sellerAgencyAssignmentRepository.findBySellerId(sellerId)
                .flatMap(existing -> {
                    applyAgencyFields(existing, organizationId, agency, now);
                    return sellerAgencyAssignmentRepository.save(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    SellerAgencyAssignment created = SellerAgencyAssignment.builder()
                            .id(UUID.randomUUID())
                            .sellerId(sellerId)
                            .createdAt(now)
                            .build();
                    applyAgencyFields(created, organizationId, agency, now);
                    return entityTemplate.insert(created);
                }))
                .map(assignment -> toAssignAgencyResponse(sellerId, assignment));
    }

    private void applyAgencyFields(SellerAgencyAssignment assignment, UUID organizationId, KernelAgencyResponse agency, LocalDateTime now) {
        assignment.setOrganizationId(organizationId);
        assignment.setAgencyId(agency.getId());
        assignment.setAgencyName(agency.getName());
        assignment.setAgencyEmail(agency.getEmail());
        assignment.setAgencyPhone(agency.getPhone());
        assignment.setAgencyCity(agency.getCity());
        assignment.setAgencyAddress(agency.getLocation());
        assignment.setUpdatedAt(now);
    }

    private AssignAgencyResponse toAssignAgencyResponse(UUID sellerId, SellerAgencyAssignment assignment) {
        AssignAgencyResponse response = new AssignAgencyResponse();
        response.setSellerId(sellerId);
        response.setAgencyId(assignment.getAgencyId());
        response.setAgency(assignment.getAgencyName());
        response.setAgencyEmail(assignment.getAgencyEmail());
        response.setAgencyPhone(assignment.getAgencyPhone());
        response.setAgencyCity(assignment.getAgencyCity());
        response.setAgencyAddress(assignment.getAgencyAddress());
        return response;
    }

    @Override
    public Mono<AssignAgencyResponse> getAgencyForSeller(UUID sellerId) {
        return sellerAgencyAssignmentRepository.findBySellerId(sellerId)
                .map(assignment -> toAssignAgencyResponse(sellerId, assignment))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No agency assigned to seller: " + sellerId)));
    }

    @Override
    public Flux<AssignAgencyResponse> listAgencyAssignments(UUID organizationId) {
        return sellerAgencyAssignmentRepository.findByOrganizationId(organizationId)
                .map(assignment -> toAssignAgencyResponse(assignment.getSellerId(), assignment));
    }

    /**
     * Same join pattern as getCustomersForSeller/getProducersForSeller
     * (assignment ids -> fetch full entities).
     */
    @Override
    public Flux<SellerListItemResponse> getSellersForAgency(UUID agencyId, UUID organizationId) {
        return sellerAgencyAssignmentRepository.findByAgencyId(agencyId)
                .map(SellerAgencyAssignment::getSellerId)
                .collectList()
                .flatMapMany(sellerIds -> sellerLocalService.findByIds(organizationId, sellerIds)
                        .map(this::toSellerListItemResponse));
    }

    @Override
    public Mono<Void> unassignAgency(UUID sellerId) {
        return sellerAgencyAssignmentRepository.findBySellerId(sellerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No agency assigned to seller: " + sellerId)))
                .flatMap(sellerAgencyAssignmentRepository::delete);
    }

    @Override
    public Mono<SellerUIPermissionsResponse> getUIPermissions(UUID sellerId) {
        return sellerLocalService.findById(sellerId)
                .flatMap(seller -> "OWNER".equals(seller.getRole())
                        ? Mono.just(sellerUIPermissionsLocalService.fullAccess(sellerId))
                        : sellerUIPermissionsLocalService.get(sellerId))
                .switchIfEmpty(sellerUIPermissionsLocalService.get(sellerId));
    }

    @Override
    public Mono<SellerUIPermissionsResponse> setUIPermissions(UUID sellerId, SellerUIPermissionsRequest request) {
        return ReactiveOrganizationContext.getOrganizationId()
                .flatMap(organizationId -> sellerUIPermissionsLocalService.set(sellerId, organizationId, null, request));
    }

    @Override
    public Mono<SellerListItemResponse> updatePermissions(UUID sellerId, UpdateSellerPermissionsRequest request) {
        List<String> permissions = request.getPermissions() == null ? List.of()
                : request.getPermissions().stream().map(Enum::name).toList();
        List<String> permittedSaleSizes = request.getPermittedSaleSizes() == null ? List.of()
                : request.getPermittedSaleSizes().stream().map(Enum::name).toList();
        return sellerLocalService.updatePermissions(sellerId, permissions, permittedSaleSizes)
                .map(this::toSellerListItemResponse);
    }

    @Override
    public Mono<SellerListItemResponse> updatePhoto(UUID sellerId, UpdateSellerPhotoRequest request) {
        return sellerLocalService.updatePhoto(sellerId, request.getProfileImageUrl())
                .map(this::toSellerListItemResponse);
    }

    @Override
    public Mono<SellerListItemResponse> updateRole(UUID sellerId, UpdateSellerRoleRequest request) {
        return sellerLocalService.updateRole(sellerId, request.getRole())
                .map(this::toSellerListItemResponse);
    }

    /**
     * Owners provisioned via Try Out/login auto-provisioning never go through
     * saveAuthContext (see AuthUseCaseImpl.provisionOwnerSeller), so they can
     * reach here with no auth-context row yet — upsert like upsertAssignment
     * above rather than requiring one to already exist.
     */
    @Override
    public Mono<SellerListItemResponse> regeneratePin(UUID sellerId) {
        return sellerLocalService.findById(sellerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found: " + sellerId)))
                .flatMap(seller -> {
                    String pin = generatePin();
                    return upsertPin(sellerId, seller.getOrganizationId(), pin)
                            .thenReturn(toSellerListItemResponse(seller))
                            .map(response -> {
                                response.setPin(pin);
                                return response;
                            });
                });
    }

    private Mono<SellerAuthContext> upsertPin(UUID sellerId, UUID organizationId, String pin) {
        LocalDateTime now = LocalDateTime.now();
        return sellerAuthContextRepository.findBySellerId(sellerId)
                .flatMap(existing -> {
                    existing.setPin(pin);
                    existing.setUpdatedAt(now);
                    return sellerAuthContextRepository.save(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    SellerAuthContext context = SellerAuthContext.builder()
                            .id(UUID.randomUUID())
                            .sellerId(sellerId)
                            .organizationId(organizationId)
                            .pin(pin)
                            .status("ACTIVE")
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return entityTemplate.insert(context);
                }));
    }

    /** Also cleans up the seller's agency assignment and PIN/auth context — orphaned rows there caused real confusion before. */
    @Override
    public Mono<Void> deleteSeller(UUID sellerId) {
        return sellerAgencyAssignmentRepository.findBySellerId(sellerId)
                .flatMap(sellerAgencyAssignmentRepository::delete)
                .then(sellerAuthContextRepository.findBySellerId(sellerId)
                        .flatMap(sellerAuthContextRepository::delete))
                .then(sellerLocalService.delete(sellerId));
    }
}
