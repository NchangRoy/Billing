package com.example.account.modules.tiers.adapter.output.external;

import com.example.account.modules.tiers.domain.port.output.ClientRepositoryPort;
import com.example.account.modules.tiers.domain.model.Client;
import com.example.account.modules.tiers.domain.model.enums.TypeClient;
import com.example.account.modules.shared.dto.kernel.ThirdPartyResponse;
import com.example.account.modules.shared.dto.kernel.ApiResponseListThirdPartyResponse;
import com.example.account.modules.shared.dto.kernel.ApiResponseThirdPartyResponse;
import com.example.account.modules.core.context.ReactiveOrganizationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KernelClientAdapter implements ClientRepositoryPort {

    private final WebClient.Builder webClientBuilder;

    @Value("${comops.kernel.ip}")
    private String kernelIp;

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
        String url = String.format("http://%s/api/clients/%s", kernelIp, id);
        return getOrganizationId().flatMap(orgId ->
            webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header("X-Organization-ID", orgId.toString())
                    .retrieve()
                    .bodyToMono(ApiResponseThirdPartyResponse.class)
                    .map(ApiResponseThirdPartyResponse::getData)
                    .map(this::mapThirdPartyToClient)
        );
    }

    @Override
    public Mono<Client> findByUsername(String username) {
        return findAllActiveClients()
                .filter(c -> username.equalsIgnoreCase(c.getUsername()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                    "Client non trouvé avec username: " + username)));
    }

    @Override
    public Mono<Client> findByEmail(String email) {
        return findAllActiveClients()
                .filter(c -> email.equalsIgnoreCase(c.getEmail()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                    "Client non trouvé avec email: " + email)));
    }

    @Override
    public Mono<Client> findByCodeClient(String codeClient) {
        return findAllActiveClients()
                .filter(c -> codeClient.equalsIgnoreCase(c.getCodeClient()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                    "Client non trouvé avec code: " + codeClient)));
    }

    @Override
    public Flux<Client> findByTypeClient(TypeClient typeClient) {
        return findAllActiveClients()
                .filter(c -> typeClient == c.getTypeClient());
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
        return getOrganizationId().flatMapMany(orgId -> {
            String url = String.format("http://%s/api/clients?organizationId=%s", kernelIp, orgId);
            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header("X-Organization-ID", orgId.toString())
                    .retrieve()
                    .bodyToMono(ApiResponseListThirdPartyResponse.class)
                    .flatMapMany(response -> Flux.fromIterable(response.getData()))
                    .map(this::mapThirdPartyToClient);
        });
    }

    @Override
    public Mono<Long> countActiveClients() {
        return findAllActiveClients().count();
    }

    @Override
    public Mono<Client> save(Client client) {
        return Mono.error(new UnsupportedOperationException(
            "La création/modification de clients est gérée par le Kernel"));
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return Mono.error(new UnsupportedOperationException(
            "La suppression de clients est gérée par le Kernel"));
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return findById(id).map(c -> true).onErrorReturn(false);
    }

    @Override
    public Mono<Long> count() {
        return findAllActiveClients().count();
    }

    private Client mapThirdPartyToClient(ThirdPartyResponse tp) {
        Client client = new Client();
        client.setIdClient(tp.getId());
        client.setOrganizationId(tp.getOrganizationId() != null ? tp.getOrganizationId() : tp.getTenantId());
        client.setUsername(tp.getDisplayName());
        client.setRaisonSociale(tp.getName());
        client.setEmail(null); // non fourni dans ThirdPartyResponse
        client.setTelephone(null); // non fourni
        client.setActif(tp.getActive() != null ? tp.getActive() : (tp.getEnabled() != null ? tp.getEnabled() : true));
        client.setLimiteCredit(tp.getAuthorizedCreditLimit() != null ? tp.getAuthorizedCreditLimit() : 0.0);
        client.setNumeroTva(tp.getTaxNumber());
        client.setCodeClient(tp.getCode() != null ? tp.getCode() : tp.getReferenceCode());
        client.setSoldeCourant(tp.getOperationsBalance() != null ? tp.getOperationsBalance() : 0.0);
        client.setCategorie(tp.getThirdPartyFamily());
        
        // determine client type if possible
        if ("ACTOR".equalsIgnoreCase(tp.getPartyType())) {
            client.setTypeClient(TypeClient.PARTICULIER);
        } else {
            client.setTypeClient(TypeClient.ENTREPRISE);
        }
        
        return client;
    }
}
