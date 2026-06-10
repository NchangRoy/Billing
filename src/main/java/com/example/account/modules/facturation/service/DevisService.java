package com.example.account.modules.facturation.service;

import com.example.account.modules.facturation.dto.request.DevisCreateRequest;
import com.example.account.modules.facturation.dto.request.ExternalRequest.EmailRequest;
import com.example.account.modules.facturation.dto.response.DevisResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import com.example.account.modules.facturation.mapper.DevisMapper;
import com.example.account.modules.facturation.model.entity.Devis;
import com.example.account.modules.facturation.model.entity.Others.PortalAccessToken;
import com.example.account.modules.facturation.model.enums.StatutDevis;
import com.example.account.modules.facturation.repository.DevisRepository;
import com.example.account.modules.facturation.service.ExternalServices.PortalAccessService;
import com.example.account.modules.facturation.service.ExternalServices.PortalTokenService;
import com.example.account.modules.facturation.service.ExternalServices.SellerService;
import com.example.account.modules.facturation.service.ExternalServices.entity.PortalPermissions;
import com.example.account.modules.facturation.service.ExternalServices.entity.enums.ResourceType;
import com.example.account.modules.facturation.service.producer.DevisEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ObjectInputFilter.Status;
import java.security.Permission;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DevisService {

    private final DevisRepository devisRepository;
    private final DevisMapper devisMapper;
    private final DevisEventProducer devisEventProducer;
    private final SellerService sellerService;
    private final R2dbcEntityTemplate entityTemplate;
    private final EmailService emailService;
    private final PortalTokenService portalTokenService;
    private final BonCommandeService bonCommandeService;

    @Transactional
    public Mono<DevisResponse> createDevis(DevisCreateRequest request) {
        log.info("Création d'un nouveau devis pour le client: {}", request.getIdClient());

        Devis devis = devisMapper.toEntity(request);
        if (devis.getIdDevis() == null) {
            devis.setIdDevis(UUID.randomUUID());
        }
        
        devis.setUpdatedAt(LocalDateTime.now());

        return entityTemplate.insert(devis)
                .map(savedDevis -> {
                    DevisResponse response = devisMapper.toResponse(savedDevis);
                    devisEventProducer.publishDevisCreated(response);
                    log.info("Devis créé avec succès: {}", savedDevis.getNumeroDevis());
                    return response;
                });
    }

    @Transactional
    public Mono<DevisResponse> updateDevis(UUID devisId, DevisCreateRequest request) {
        log.info("Mise à jour du devis: {}", devisId);

        return devisRepository.findById(devisId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Devis non trouvé: " + devisId)))
                .flatMap(devis -> {
                    devisMapper.updateEntityFromRequest(request, devis);
                    devis.setUpdatedAt(LocalDateTime.now());
                    return devisRepository.save(devis);
                })
                .map(updatedDevis -> {
                    DevisResponse response = devisMapper.toResponse(updatedDevis);
                    devisEventProducer.publishDevisUpdated(response);
                    log.info("Devis mis à jour avec succès: {}", devisId);
                    return response;
                });
    }

    @Transactional(readOnly = true)
    public Mono<DevisResponse> getDevisById(UUID devisId) {
        log.info("Récupération du devis: {}", devisId);

        return devisRepository.findById(devisId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Devis non trouvé: " + devisId)))
                .map(devisMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Mono<DevisResponse> getDevisByNumero(String numeroDevis) {
        log.info("Récupération du devis par numéro: {}", numeroDevis);

        return devisRepository.findByNumeroDevis(numeroDevis)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Devis non trouvé avec numéro: " + numeroDevis)))
                .map(devisMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<DevisResponse> getAllDevis() {
        log.info("Récupération de tous les devis");
        return devisRepository.findAll()
                .map(devisMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<DevisResponse> getAllDevis(Pageable pageable) {
        log.info("Récupération de tous les devis avec pagination");
        return devisRepository.findAll()
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .map(devisMapper::toResponse);
    }

  
 @Transactional
public Mono<Void> sendDevisAsEmail(EmailRequest emailRequest) {
    log.info("Processing email request for ID: {}", emailRequest.getId());

    return devisRepository.findById(emailRequest.getId())
        .switchIfEmpty(Mono.error(new RuntimeException("Quotation not found with ID: " + emailRequest.getId())))
        .flatMap(devis -> {
            log.info("Found quotation, generating token and sending email");
            //first build permssion object
            PortalPermissions permissions=PortalPermissions.builder()
                                                            .canAccept(emailRequest.getCanAccept())
                                                            .canModify(emailRequest.getCanModify())
                                                            .canReject(emailRequest.getCanReject())
                                                            .canView(emailRequest.getCanView())
                                                            .build();
            // 1. Create the token first
            return portalTokenService.createToken(devis.getIdDevis(), ResourceType.QUOTATION, devis.getEmailClient(),permissions)
                .flatMap(token -> 
                    // 2. Send the email using the token
                    emailService.sendQuotation(devis, emailRequest, token.getToken())
                )
                .then(Mono.defer(() -> {
                    // 3. ONLY if email succeeds, update status and save
                    devis.setStatut(StatutDevis.ENVOYE);
                    return devisRepository.save(devis);
                }))
                .then(); // Convert Mono<Devis> to Mono<Void>
        })
        .onErrorResume(e -> {
            log.error("Failed to process email request for quotation: {}", emailRequest.getId(), e);
            return Mono.error(new RuntimeException("Email service failed: " + e.getMessage()));
        });
}

    @Transactional
    public Mono<Void> deleteDevis(UUID devisId) {
        log.info("Suppression du devis: {}", devisId);

        return devisRepository.existsById(devisId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException("Devis non trouvé: " + devisId));
                    }
                    return devisRepository.deleteById(devisId)
                            .then(Mono.fromRunnable(() -> devisEventProducer.publishDevisDeleted(devisId)));
                })
                .then();
    }


@Transactional
public Mono<Void> accepterDevis(UUID devisId) {
    log.info("Acceptation du devis: {}", devisId);

    return devisRepository.findById(devisId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Devis non trouvé: " + devisId)))
            .flatMap(devis -> {
                // 1. Update the Devis status
                devis.setStatut(StatutDevis.ACCEPTE);
                devis.setDateAcceptation(LocalDateTime.now());
                return devisRepository.save(devis);
            })
            .flatMap(updatedDevis -> {
                // 2. Chain the Order Creation (Sales Order / Bon de Commande)
                // This ensures the BC is created BEFORE we signal completion
                return bonCommandeService.createFromQuotation(updatedDevis)
                        .then(Mono.just(updatedDevis)); 
            })
            .doOnNext(updatedDevis -> {
                // 3. Side effects: Events and Logging
                DevisResponse response = devisMapper.toResponse(updatedDevis);
                devisEventProducer.publishDevisAccepted(response);
                log.info("Devis {} marqué comme ACCEPTE, BC créé et événement publié", devisId);
            })
            .then();
}


@Transactional
public Mono<Void> refuserDevis(UUID devisId) {
    log.info("Refus du devis: {}", devisId);

    return devisRepository.findById(devisId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Devis non trouvé: " + devisId)))
            .flatMap(devis -> {
                devis.setStatut(StatutDevis.REFUSE);
                devis.setDateRefus(LocalDateTime.now());
                return devisRepository.save(devis);
            })
            .doOnNext(updatedDevis -> {
                // Consistency: Log the refusal or publish a "Rejected" event if needed
                log.info("Devis {} marqué comme REFUSE", devisId);
            })
            .then(); // Discards the result and returns Mono<Void>
}

    public Flux<SellerAuthResponse> enrichDevis(UUID orgId) {
        return sellerService.getSellersByOrganization(orgId);
    }
}
