package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.core.exception.SalesCoreErrorMapper;
import com.example.account.modules.facturation.domain.model.Facture;
import com.example.account.modules.facturation.domain.model.LigneFacture;
import com.example.account.modules.facturation.domain.port.output.FactureRepositoryPort;
import com.example.account.modules.facturation.dto.request.FactureCreateRequest;
import com.example.account.modules.facturation.dto.request.LigneFactureCreateRequest;
import com.example.account.modules.facturation.dto.response.FactureResponse;
import com.example.account.modules.facturation.model.enums.StatutFacture;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Primary
public class FacturePersistenceAdapter implements FactureRepositoryPort {

    private final WebClient salesCoreWebClient;
    private final ObjectMapper objectMapper;

    public FacturePersistenceAdapter(@Qualifier("salesCoreWebClient") WebClient salesCoreWebClient,
                                     ObjectMapper objectMapper) {
        this.salesCoreWebClient = salesCoreWebClient;
        this.objectMapper = objectMapper;
    }

    private static final Function<ClientResponse, Mono<? extends Throwable>> SALES_CORE_ERROR =
            SalesCoreErrorMapper.forContext("sales-core persistence error");

    private Facture toDomain(FactureResponse response) {
        if (response == null) return null;
        Facture domain = objectMapper.convertValue(response, Facture.class);
        if (response.getIdFacture() != null) {
            domain.setIdFacture(UUID.fromString(response.getIdFacture()));
        }
        if (response.getIdClient() != null) {
            domain.setIdClient(UUID.fromString(response.getIdClient()));
        }
        return domain;
    }

    private FactureCreateRequest toCreateRequest(Facture f) {
        if (f == null) return null;
        return FactureCreateRequest.builder()
                .numeroFacture(f.getNumeroFacture())
                .dateFacturation(f.getDateFacturation())
                .dateEcheance(f.getDateEcheance())
                .dateSysteme(f.getDateSysteme())
                .type(f.getType())
                .etat(f.getEtat())
                .idClient(f.getIdClient())
                .nomClient(f.getNomClient())
                .adresseClient(f.getAdresseClient())
                .emailClient(f.getEmailClient())
                .telephoneClient(f.getTelephoneClient())
                .lignesFacture(toLignesRequest(f.getLignesFacture()))
                .montantHT(f.getMontantHT())
                .montantTVA(f.getMontantTVA())
                .montantTTC(f.getMontantTTC())
                .montantTotal(f.getMontantTotal())
                .finalAmount(f.getFinalAmount())
                .montantRestant(f.getMontantRestant())
                .applyVat(f.getApplyVat())
                .devise(f.getDevise())
                .tauxChange(f.getTauxChange())
                .modeReglement(f.getModeReglement())
                .conditionsPaiement(f.getConditionsPaiement())
                .nbreEcheance(f.getNbreEcheance())
                .nosRef(f.getNosRef())
                .vosRef(f.getVosRef())
                .referenceCommande(f.getReferenceCommande())
                .idDevisOrigine(f.getIdDevisOrigine() != null ? UUID.fromString(f.getIdDevisOrigine()) : null)
                .notes(f.getNotes())
                .pdfPath(f.getPdfPath())
                .envoyeParEmail(f.getEnvoyeParEmail())
                .dateEnvoiEmail(f.getDateEnvoiEmail())
                .remiseGlobalePourcentage(f.getRemiseGlobalePourcentage())
                .remiseGlobaleMontant(f.getRemiseGlobaleMontant())
                .referalClientId(f.getReferalClientId())
                .organizationId(f.getOrganizationId())
                .agencyId(f.getAgencyId())
                .createdBy(f.getCreatedBy())
                .originType(f.getOriginType())
                .sessionId(f.getSessionId())
                .build();
    }

    private List<LigneFactureCreateRequest> toLignesRequest(List<LigneFacture> lignes) {
        if (lignes == null) return List.of();
        return lignes.stream()
                .map(l -> LigneFactureCreateRequest.builder()
                        .quantite(l.getQuantite() != null ? l.getQuantite().intValue() : 0)
                        .description(l.getDescription())
                        .debit(l.getDebit())
                        .credit(l.getCredit())
                        .isTaxLine(l.getIsTaxLine() != null ? l.getIsTaxLine() : false)
                        .idProduit(l.getIdProduit() != null ? UUID.fromString(l.getIdProduit()) : null)
                        .nomProduit(l.getNomProduit())
                        .prixUnitaire(l.getPrixUnitaire())
                        .montantTotal(l.getMontantTotal())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Mono<Facture> findById(UUID id) {
        return salesCoreWebClient.get()
                .uri("/api/factures/{id}", id)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(FactureResponse.class)
                .map(this::toDomain);
    }

    @Override
    public Mono<Facture> findByNumeroFacture(String numeroFacture) {
        return salesCoreWebClient.get()
                .uri("/api/factures/numero/{numero}", numeroFacture)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(FactureResponse.class)
                .map(this::toDomain);
    }

    @Override
    public Flux<Facture> findByIdClient(UUID idClient) {
        return salesCoreWebClient.get()
                .uri("/api/factures/client/{clientId}", idClient)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .map(this::toDomain);
    }

    @Override
    public Flux<Facture> findByEtat(StatutFacture etat) {
        return salesCoreWebClient.get()
                .uri("/api/factures/etat/{etat}", etat.name())
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .map(this::toDomain);
    }

    @Override
    public Flux<Facture> findByType(String type) {
        return salesCoreWebClient.get()
                .uri("/api/factures")
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .filter(res -> type == null || type.equalsIgnoreCase(res.getType()))
                .map(this::toDomain);
    }

    @Override
    public Flux<Facture> findByClientAndEtat(UUID idClient, StatutFacture etat) {
        return findByIdClient(idClient)
                .filter(f -> etat == f.getEtat());
    }

    @Override
    public Flux<Facture> findByDateFacturationBetween(LocalDate startDate, LocalDate endDate) {
        return salesCoreWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/factures")
                        .queryParam("dateDebut", startDate.toString())
                        .queryParam("dateFin", endDate.toString())
                        .build())
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .filter(res -> {
                    if (res.getDateFacturation() == null) return false;
                    LocalDate d = res.getDateFacturation().toLocalDate();
                    return !d.isBefore(startDate) && !d.isAfter(endDate);
                })
                .map(this::toDomain);
    }

    @Override
    public Flux<Facture> findByDateEcheanceBetween(LocalDate startDate, LocalDate endDate) {
        return salesCoreWebClient.get()
                .uri("/api/factures")
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .filter(res -> {
                    if (res.getDateEcheance() == null) return false;
                    LocalDate d = res.getDateEcheance().toLocalDate();
                    return !d.isBefore(startDate) && !d.isAfter(endDate);
                })
                .map(this::toDomain);
    }

    @Override
    public Flux<Facture> findOverdueFactures(LocalDate currentDate) {
        return salesCoreWebClient.get()
                .uri("/api/factures/en-retard")
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .map(this::toDomain);
    }

    @Override
    public Flux<Facture> findByMontantTotalBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return salesCoreWebClient.get()
                .uri("/api/factures")
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .filter(res -> {
                    BigDecimal total = res.getMontantTotal();
                    if (total == null) return false;
                    return total.compareTo(minAmount) >= 0 && total.compareTo(maxAmount) <= 0;
                })
                .map(this::toDomain);
    }

    @Override
    public Flux<Facture> findUnpaidFactures() {
        return salesCoreWebClient.get()
                .uri("/api/factures/non-payees")
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .map(this::toDomain);
    }

    @Override
    public Flux<Facture> findByDevise(String devise) {
        return salesCoreWebClient.get()
                .uri("/api/factures")
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .filter(res -> devise == null || devise.equalsIgnoreCase(res.getDevise()))
                .map(this::toDomain);
    }

    @Override
    public Flux<Facture> findByEnvoyeParEmail(Boolean envoyeParEmail) {
        return salesCoreWebClient.get()
                .uri("/api/factures")
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .filter(res -> envoyeParEmail == null || envoyeParEmail.equals(res.getEnvoyeParEmail()))
                .map(this::toDomain);
    }

    @Override
    public Mono<Long> countByEtat(StatutFacture etat) {
        return findByEtat(etat).count();
    }

    @Override
    public Mono<Long> countByIdClient(UUID idClient) {
        return findByIdClient(idClient).count();
    }

    @Override
    public Mono<Long> countByDateFacturationBetween(LocalDate startDate, LocalDate endDate) {
        return findByDateFacturationBetween(startDate, endDate).count();
    }

    @Override
    public Mono<BigDecimal> sumMontantByDateBetween(LocalDate startDate, LocalDate endDate) {
        return findByDateFacturationBetween(startDate, endDate)
                .map(f -> f.getMontantTotal() != null ? f.getMontantTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Mono<BigDecimal> sumMontantByEtat(StatutFacture etat) {
        return findByEtat(etat)
                .map(f -> f.getMontantTotal() != null ? f.getMontantTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Mono<Long> countByStatut(String statut) {
        try {
            StatutFacture state = StatutFacture.valueOf(statut);
            return countByEtat(state);
        } catch (IllegalArgumentException e) {
            return Mono.just(0L);
        }
    }

    @Override
    public Mono<BigDecimal> sumMontantByStatut(String statut) {
        try {
            StatutFacture state = StatutFacture.valueOf(statut);
            return sumMontantByEtat(state);
        } catch (IllegalArgumentException e) {
            return Mono.just(BigDecimal.ZERO);
        }
    }

    @Override
    public Mono<Long> countByDateBetween(LocalDate startDate, LocalDate endDate) {
        return findByDateFacturationBetween(startDate, endDate).count();
    }

    @Override
    public Mono<Boolean> existsByNumeroFacture(String numeroFacture) {
        return findByNumeroFacture(numeroFacture)
                .map(f -> true)
                .defaultIfEmpty(false)
                .onErrorReturn(false);
    }

    @Override
    public Flux<Facture> findByOrganizationId(UUID organizationId) {
        return salesCoreWebClient.get()
                .uri("/api/factures/organisation/{organizationId}", organizationId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .map(this::toDomain);
    }

    @Override
    public Flux<Facture> findByAgencyId(UUID agencyId) {
        return salesCoreWebClient.get()
                .uri("/api/factures/agence/{agencyId}", agencyId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .map(this::toDomain);
    }

    @Override
    public Flux<Facture> findByCreatedBy(UUID createdBy) {
        return salesCoreWebClient.get()
                .uri("/api/factures/vendeur/{sellerId}", createdBy)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .map(this::toDomain);
    }

    @Override
    public Mono<Facture> save(Facture facture) {
        FactureCreateRequest request = toCreateRequest(facture);
        if (facture.getIdFacture() != null) {
            return salesCoreWebClient.put()
                    .uri("/api/factures/{id}", facture.getIdFacture())
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                    .bodyToMono(FactureResponse.class)
                    .map(this::toDomain);
        } else {
            return insert(facture);
        }
    }

    @Override
    public Mono<Facture> insert(Facture facture) {
        FactureCreateRequest request = toCreateRequest(facture);
        return salesCoreWebClient.post()
                .uri("/api/factures")
                .bodyValue(request)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(FactureResponse.class)
                .map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return salesCoreWebClient.delete()
                .uri("/api/factures/{id}", id)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return findById(id)
                .map(f -> true)
                .defaultIfEmpty(false)
                .onErrorReturn(false);
    }

    @Override
    public Flux<Facture> findAll() {
        return salesCoreWebClient.get()
                .uri("/api/factures")
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToFlux(FactureResponse.class)
                .map(this::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return findAll().count();
    }

    @Override
    public Mono<Map<String, Object>> getAccountingSaleFacture(UUID factureId) {
        return salesCoreWebClient.get()
                .uri("/api/factures/sales/{id}", factureId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    @Override
    public Mono<Map<String, Object>> getAccountingPurchaseFacture(UUID factureId) {
        return salesCoreWebClient.get()
                .uri("/api/factures/purchases/{id}", factureId)
                .retrieve()
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), SALES_CORE_ERROR)
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
