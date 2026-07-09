package com.example.account.modules.tiers.adapter.output.external;

import com.example.account.modules.core.context.ReactiveOrganizationContext;
import com.example.account.modules.shared.dto.kernel.SalesCoreClientResponse;
import com.example.account.modules.tiers.domain.model.Client;
import com.example.account.modules.tiers.domain.model.enums.TypeClient;
import com.example.account.modules.tiers.domain.port.output.ClientRepositoryPort;
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
public class KernelClientAdapter implements ClientRepositoryPort {

    private final WebClient salesCoreWebClient;

    public KernelClientAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient) {
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
    public Mono<Client> findById(UUID id) {
        return salesCoreWebClient
                .get()
                .uri("/api/customers/{id}", id)
                .retrieve()
                .bodyToMono(SalesCoreClientResponse.class)
                .map(this::mapToClient);
    }

    @Override
    public Mono<Client> findByUsername(String username) {
        return findAllActiveClients()
                .filter(c -> username.equalsIgnoreCase(c.getUsername()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Client non trouvé avec username: " + username)));
    }

    @Override
    public Mono<Client> findByEmail(String email) {
        return findAllActiveClients()
                .filter(c -> email.equalsIgnoreCase(c.getEmail()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Client non trouvé avec email: " + email)));
    }

    @Override
    public Mono<Client> findByCodeClient(String codeClient) {
        return findAllActiveClients()
                .filter(c -> codeClient.equalsIgnoreCase(c.getCodeClient()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Client non trouvé avec code: " + codeClient)));
    }

    @Override
    public Flux<Client> findByTypeClient(TypeClient typeClient) {
        return findAllActiveClients().filter(c -> typeClient == c.getTypeClient());
    }

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return findByUsername(username).map(c -> true).onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return findByEmail(email).map(c -> true).onErrorReturn(false);
    }

    @Override
    public Flux<Client> findAllActiveClients() {
        return getOrganizationId().flatMapMany(orgId ->
                salesCoreWebClient
                        .get()
                        .uri("/api/customers")
                        .header("X-Organization-Id", orgId.toString())
                        .retrieve()
                        .bodyToFlux(SalesCoreClientResponse.class)
                        .map(c -> mapToClient(c, orgId))
        );
    }

    @Override
    public Mono<Long> countActiveClients() {
        return findAllActiveClients().count();
    }

    @Override
    public Mono<Client> save(Client client) {
        return Mono.error(new UnsupportedOperationException("La création/modification de clients est gérée par le Kernel"));
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return Mono.error(new UnsupportedOperationException("La suppression de clients est gérée par le Kernel"));
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return findById(id).map(c -> true).onErrorReturn(false);
    }

    @Override
    public Mono<Long> count() {
        return findAllActiveClients().count();
    }

    @Override
    public Mono<Void> resendCredentials(UUID id, String email, String name) {
        return getOrganizationId().flatMap(orgId ->
                salesCoreWebClient
                        .post()
                        .uri("/api/customers/{id}/invite", id)
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
                        .uri("/api/customers/{id}/ensure-portal-access", id)
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

    private Client mapToClient(SalesCoreClientResponse c) {
        return mapToClient(c, null);
    }

    private Client mapToClient(SalesCoreClientResponse c, UUID organizationId) {
        Client client = new Client();
        client.setIdClient(c.getIdClient());
        client.setOrganizationId(organizationId);
        client.setUsername(c.getUsername());
        client.setCategorie(c.getCategorie());
        client.setSiteWeb(c.getSiteWeb());
        client.setNTva(c.getNtva() != null ? c.getNtva() : false);
        client.setAllowedSaleSizes(c.getAllowedSaleSizes());
        client.setAdresse(c.getAdresse());
        client.setTelephone(c.getTelephone());
        client.setEmail(c.getEmail());
        client.setTypeClient("ADMINISTRATION".equalsIgnoreCase(c.getTypeClient()) ? TypeClient.ADMINISTRATION
                : "ENTREPRISE".equalsIgnoreCase(c.getTypeClient()) ? TypeClient.ENTREPRISE
                : TypeClient.PARTICULIER);
        client.setRaisonSociale(c.getRaisonSociale());
        client.setNumeroTva(c.getNumeroTva());
        client.setCodeClient(c.getCodeClient());
        client.setLimiteCredit(c.getLimiteCredit() != null ? c.getLimiteCredit() : 0.0);
        client.setSoldeCourant(c.getSoldeCourant() != null ? c.getSoldeCourant() : 0.0);
        client.setActif(c.getActif() != null ? c.getActif() : true);
        return client;
    }
}
