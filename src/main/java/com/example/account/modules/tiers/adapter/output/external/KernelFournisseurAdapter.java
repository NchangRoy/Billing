package com.example.account.modules.tiers.adapter.output.external;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.shared.dto.kernel.SalesCoreClientResponse;
import com.example.account.modules.tiers.domain.model.Fournisseur;
import com.example.account.modules.tiers.domain.model.enums.TypeClient;
import com.example.account.modules.tiers.domain.port.output.FournisseurRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class KernelFournisseurAdapter implements FournisseurRepositoryPort {

    private final WebClient salesCoreWebClient;

    public KernelFournisseurAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient) {
        this.salesCoreWebClient = salesCoreWebClient;
    }

    private Mono<UUID> getOrganizationId() {
        return Mono.deferContextual(ctx -> {
            UUID orgId = ctx.getOrDefault(ReactiveOrganizationContext.ORGANIZATION_ID_KEY, null);
            if (orgId == null) {
                return Mono.error(new IllegalStateException("Organization ID absent du contexte réactif"));
            }
            return Mono.just(orgId);
        });
    }

    @Override
    public Mono<Fournisseur> findById(UUID id) {
        return salesCoreWebClient
                .get()
                .uri("/api/fournisseurs/{id}", id)
                .retrieve()
                .bodyToMono(SalesCoreClientResponse.class)
                .map(this::mapToFournisseur);
    }

    @Override
    public Mono<Fournisseur> findByUsername(String username) {
        return findAllActiveFournisseurs()
                .filter(f -> username.equalsIgnoreCase(f.getUsername()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Fournisseur non trouvé avec username: " + username)));
    }

    @Override
    public Mono<Fournisseur> findByEmail(String email) {
        return findAllActiveFournisseurs()
                .filter(f -> email.equalsIgnoreCase(f.getEmail()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Fournisseur non trouvé avec email: " + email)));
    }

    @Override
    public Mono<Fournisseur> findByCodeFournisseur(String codeFournisseur) {
        return findAllActiveFournisseurs()
                .filter(f -> codeFournisseur.equalsIgnoreCase(f.getCodeFournisseur()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Fournisseur non trouvé avec code: " + codeFournisseur)));
    }

    @Override
    public Flux<Fournisseur> findByTypeFournisseur(TypeClient typeFournisseur) {
        return findAllActiveFournisseurs().filter(f -> typeFournisseur == f.getTypeFournisseur());
    }

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return findByUsername(username).map(f -> true).onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return findByEmail(email).map(f -> true).onErrorReturn(false);
    }

    @Override
    public Flux<Fournisseur> findAllActiveFournisseurs() {
        return getOrganizationId().flatMapMany(orgId ->
                salesCoreWebClient
                        .get()
                        .uri("/api/fournisseurs")
                        .header("X-Organization-Id", orgId.toString())
                        .retrieve()
                        .bodyToFlux(SalesCoreClientResponse.class)
                        .map(f -> mapToFournisseur(f, orgId))
        );
    }

    @Override
    public Mono<Long> countActiveFournisseurs() {
        return findAllActiveFournisseurs().count();
    }

    @Override
    public Mono<Fournisseur> save(Fournisseur fournisseur) {
        return Mono.error(new UnsupportedOperationException("La création/modification de fournisseurs est gérée par le Kernel"));
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return Mono.error(new UnsupportedOperationException("La suppression de fournisseurs est gérée par le Kernel"));
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return findById(id).map(f -> true).onErrorReturn(false);
    }

    @Override
    public Mono<Void> resendCredentials(UUID id, String email, String name) {
        return getOrganizationId().flatMap(orgId ->
                salesCoreWebClient
                        .post()
                        .uri("/api/fournisseurs/{id}/invite", id)
                        .bodyValue(Map.of(
                                "clientId", id.toString(),
                                "organizationId", orgId.toString(),
                                "email", email,
                                "name", name
                        ))
                        .retrieve()
                        .bodyToMono(Void.class)
        );
    }

    @Override
    public Mono<Void> ensurePortalAccess(UUID id, String email, String name) {
        return getOrganizationId().flatMap(orgId ->
                salesCoreWebClient
                        .post()
                        .uri("/api/fournisseurs/{id}/ensure-portal-access", id)
                        .bodyValue(Map.of(
                                "clientId", id.toString(),
                                "organizationId", orgId.toString(),
                                "email", email,
                                "name", name
                        ))
                        .retrieve()
                        .bodyToMono(Boolean.class)
        ).then();
    }

    private Fournisseur mapToFournisseur(SalesCoreClientResponse c) {
        return mapToFournisseur(c, null);
    }

    private Fournisseur mapToFournisseur(SalesCoreClientResponse c, UUID organizationId) {
        Fournisseur f = new Fournisseur();
        f.setIdFournisseur(c.getIdClient());
        f.setOrganizationId(organizationId);
        f.setUsername(c.getUsername());
        f.setCategorie(c.getCategorie());
        f.setSiteWeb(c.getSiteWeb());
        f.setNTva(c.getNtva() != null ? c.getNtva() : false);
        f.setAllowedSaleSizes(c.getAllowedSaleSizes());
        f.setAdresse(c.getAdresse());
        f.setTelephone(c.getTelephone());
        f.setEmail(c.getEmail());
        f.setRaisonSociale(c.getRaisonSociale());
        f.setNumeroTva(c.getNumeroTva());
        f.setCodeFournisseur(c.getCodeClient());
        f.setLimiteCredit(c.getLimiteCredit() != null ? c.getLimiteCredit() : 0.0);
        f.setSoldeCourant(c.getSoldeCourant() != null ? c.getSoldeCourant() : 0.0);
        f.setActif(c.getActif() != null ? c.getActif() : true);
        f.setTypeFournisseur("ADMINISTRATION".equalsIgnoreCase(c.getTypeClient()) ? TypeClient.ADMINISTRATION
                : "ENTREPRISE".equalsIgnoreCase(c.getTypeClient()) ? TypeClient.ENTREPRISE
                : TypeClient.PARTICULIER);
        return f;
    }
}
