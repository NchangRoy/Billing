package com.example.account.modules.tiers.adapter.output.external;

import com.example.account.modules.tiers.domain.port.output.FournisseurRepositoryPort;
import com.example.account.modules.tiers.domain.model.Fournisseur;
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
public class KernelFournisseurAdapter implements FournisseurRepositoryPort {

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
    public Mono<Fournisseur> findById(UUID id) {
        String url = String.format("http://%s/api/suppliers/%s", kernelIp, id);
        return getOrganizationId().flatMap(orgId ->
            webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header("X-Organization-ID", orgId.toString())
                    .retrieve()
                    .bodyToMono(ApiResponseThirdPartyResponse.class)
                    .map(ApiResponseThirdPartyResponse::getData)
                    .map(this::mapThirdPartyToFournisseur)
        );
    }

    @Override
    public Mono<Fournisseur> findByUsername(String username) {
        return findAllActiveFournisseurs()
                .filter(f -> username.equalsIgnoreCase(f.getUsername()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                    "Fournisseur non trouvé avec username: " + username)));
    }

    @Override
    public Mono<Fournisseur> findByEmail(String email) {
        return findAllActiveFournisseurs()
                .filter(f -> email.equalsIgnoreCase(f.getEmail()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                    "Fournisseur non trouvé avec email: " + email)));
    }

    @Override
    public Mono<Fournisseur> findByCodeFournisseur(String codeFournisseur) {
        return findAllActiveFournisseurs()
                .filter(f -> codeFournisseur.equalsIgnoreCase(f.getCodeFournisseur()))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                    "Fournisseur non trouvé avec code: " + codeFournisseur)));
    }

    @Override
    public Flux<Fournisseur> findByTypeFournisseur(TypeClient typeFournisseur) {
        return findAllActiveFournisseurs()
                .filter(f -> typeFournisseur == f.getTypeFournisseur());
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
        return getOrganizationId().flatMapMany(orgId -> {
            String url = String.format("http://%s/api/suppliers?organizationId=%s", kernelIp, orgId);
            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header("X-Organization-ID", orgId.toString())
                    .retrieve()
                    .bodyToMono(ApiResponseListThirdPartyResponse.class)
                    .flatMapMany(response -> Flux.fromIterable(response.getData()))
                    .map(this::mapThirdPartyToFournisseur);
        });
    }

    @Override
    public Mono<Long> countActiveFournisseurs() {
        return findAllActiveFournisseurs().count();
    }

    @Override
    public Mono<Fournisseur> save(Fournisseur fournisseur) {
        return Mono.error(new UnsupportedOperationException(
            "La création/modification de fournisseurs est gérée par le Kernel"));
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return Mono.error(new UnsupportedOperationException(
            "La suppression de fournisseurs est gérée par le Kernel"));
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return findById(id).map(f -> true).onErrorReturn(false);
    }

    private Fournisseur mapThirdPartyToFournisseur(ThirdPartyResponse tp) {
        Fournisseur f = new Fournisseur();
        f.setIdFournisseur(tp.getId());
        f.setOrganizationId(tp.getOrganizationId() != null ? tp.getOrganizationId() : tp.getTenantId());
        f.setUsername(tp.getDisplayName());
        f.setRaisonSociale(tp.getName());
        f.setEmail(null); // non fourni dans ThirdPartyResponse
        f.setTelephone(null); // non fourni
        f.setActif(tp.getActive() != null ? tp.getActive() : (tp.getEnabled() != null ? tp.getEnabled() : true));
        f.setLimiteCredit(tp.getAuthorizedCreditLimit() != null ? tp.getAuthorizedCreditLimit() : 0.0);
        f.setNumeroTva(tp.getTaxNumber());
        f.setCodeFournisseur(tp.getCode() != null ? tp.getCode() : tp.getReferenceCode());
        f.setSoldeCourant(tp.getOperationsBalance() != null ? tp.getOperationsBalance() : 0.0);
        f.setCategorie(tp.getThirdPartyFamily());
        // determine fournisseur type if possible
        if ("ACTOR".equalsIgnoreCase(tp.getPartyType())) {
            f.setTypeFournisseur(TypeClient.PARTICULIER);
        } else {
            f.setTypeFournisseur(TypeClient.ENTREPRISE);
        }
        return f;
    }
}
