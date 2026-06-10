package com.example.account.modules.facturation.service.ExternalServices;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.account.modules.facturation.dto.response.DevisResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.PortalAcessResponse;
import com.example.account.modules.facturation.model.entity.Others.PortalAccessToken;
import com.example.account.modules.facturation.service.DevisService;
import com.example.account.modules.facturation.service.ExternalServices.entity.PortalPermissions;
import com.example.account.modules.facturation.service.ExternalServices.entity.enums.ResourceType;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
@Slf4j
@Service
public class PortalAccessService {
    @Autowired
    private  PortalTokenService portalTokenService;
    @Autowired 
    private DevisService devisService;

  public Mono<PortalAcessResponse<DevisResponse>> getDevisInformation(String tokenValue) {
    return portalTokenService.getByTokenValue(tokenValue)
        .doOnNext(t -> log.info("Token found in DB for email: {}", t.getClientEmail()))
        .switchIfEmpty(Mono.error(new RuntimeException("Token string not found in database")))
        .flatMap(token -> {
            log.info("Fetching Devis ID: {}", token.getResourceId());
            
            return devisService.getDevisById(token.getResourceId())
                .map(devis -> {
                    // Initialize inside to ensure thread safety
                    PortalAcessResponse<DevisResponse> portalAccess = new PortalAcessResponse<>();
                    portalAccess.setData(devis);
                    
                    portalAccess.setCanAccept(token.getCanAccept());
                    portalAccess.setCanModify(token.getCanModify());
                    portalAccess.setCanView(token.getCanView());
                    portalAccess.setCanReject(token.getCanReject());
                    
                    return portalAccess;
                });
        })
        .doOnNext(res -> log.info("Found Devis Data for: {}", res.getData().getNumeroDevis()));
}
    
    public Mono<PortalAccessToken> createAccessSession(ResourceType resourceType,UUID resourceId,String email,PortalPermissions permissions){
        //create and return a token

        return portalTokenService.createToken(resourceId, resourceType, email,permissions);
    }
public Mono<Void> handleAction(String tokenValue, String action) {
    return portalTokenService.getByTokenValue(tokenValue)
        .switchIfEmpty(Mono.error(new RuntimeException("Token string not found in database")))
        .<Void>flatMap(tokenEntity -> {
            // 1. Validation Logic
            if (tokenEntity.isUsed()) {
                return Mono.error(new RuntimeException("This link has already been used."));
            }
            if (LocalDateTime.now().isAfter(tokenEntity.getExpiresAt())) {
                return Mono.error(new RuntimeException("This link has expired."));
            }

            if (tokenEntity.getResourceType().equals(ResourceType.QUOTATION)) {
                return devisService.getDevisById(tokenEntity.getResourceId())
                    .flatMap(devis -> {
                        log.info("Processing action [{}] for Devis: {}", action, devis.getNumeroDevis());

                        // 2. Map the action to the service call
                        Mono<Void> actionMono = switch (action.toLowerCase()) {
                            case "accept" -> devisService.accepterDevis(devis.getIdDevis());
                            case "reject" -> devisService.refuserDevis(devis.getIdDevis());
                            default -> Mono.error(new IllegalArgumentException("Unknown action: " + action));
                        };

                        // 3. Chain: Mark as used ONLY after the action succeeds
                        return actionMono.then(portalTokenService.markAsUsed(tokenValue)).then();
                    });
            }
            return Mono.error(new UnsupportedOperationException("Resource type not supported for actions"));
        })
        .doOnSuccess(v -> log.info("Action {} successfully processed for token {}", action, tokenValue));
}


}
