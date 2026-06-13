package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.adapter.output.persistence.mapper.FacturePersistenceMapper;
import com.example.account.modules.facturation.domain.model.Facture;
import com.example.account.modules.facturation.domain.port.output.FactureRepositoryPort;
import com.example.account.modules.facturation.model.enums.StatutFacture;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class FacturePersistenceAdapter implements FactureRepositoryPort {

    private final FactureR2dbcRepository repository;
    private final FacturePersistenceMapper mapper;
    private final R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<Facture> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<Facture> findByNumeroFacture(String numeroFacture) {
        return repository.findByNumeroFacture(numeroFacture).map(mapper::toDomain);
    }

    @Override
    public Flux<Facture> findByIdClient(UUID idClient) {
        return repository.findByIdClient(idClient).map(mapper::toDomain);
    }

    @Override
    public Flux<Facture> findByEtat(StatutFacture etat) {
        return repository.findByEtat(etat).map(mapper::toDomain);
    }

    @Override
    public Flux<Facture> findByType(String type) {
        return repository.findByType(type).map(mapper::toDomain);
    }

    @Override
    public Flux<Facture> findByClientAndEtat(UUID idClient, StatutFacture etat) {
        return repository.findByClientAndEtat(idClient, etat).map(mapper::toDomain);
    }

    @Override
    public Flux<Facture> findByDateFacturationBetween(LocalDate startDate, LocalDate endDate) {
        return repository.findByDateFacturationBetween(startDate, endDate).map(mapper::toDomain);
    }

    @Override
    public Flux<Facture> findByDateEcheanceBetween(LocalDate startDate, LocalDate endDate) {
        return repository.findByDateEcheanceBetween(startDate, endDate).map(mapper::toDomain);
    }

    @Override
    public Flux<Facture> findOverdueFactures(LocalDate currentDate) {
        return repository.findOverdueFactures(currentDate).map(mapper::toDomain);
    }

    @Override
    public Flux<Facture> findByMontantTotalBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return repository.findByMontantTotalBetween(minAmount, maxAmount).map(mapper::toDomain);
    }

    @Override
    public Flux<Facture> findUnpaidFactures() {
        return repository.findUnpaidFactures().map(mapper::toDomain);
    }

    @Override
    public Flux<Facture> findByDevise(String devise) {
        return repository.findByDevise(devise).map(mapper::toDomain);
    }

    @Override
    public Flux<Facture> findByEnvoyeParEmail(Boolean envoyeParEmail) {
        return repository.findByEnvoyeParEmail(envoyeParEmail).map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByEtat(StatutFacture etat) {
        return repository.countByEtat(etat);
    }

    @Override
    public Mono<Long> countByIdClient(UUID idClient) {
        return repository.countByIdClient(idClient);
    }

    @Override
    public Mono<Long> countByDateFacturationBetween(LocalDate startDate, LocalDate endDate) {
        return repository.countByDateFacturationBetween(startDate, endDate);
    }

    @Override
    public Mono<BigDecimal> sumMontantByDateBetween(LocalDate startDate, LocalDate endDate) {
        return repository.sumMontantByDateBetween(startDate, endDate);
    }

    @Override
    public Mono<BigDecimal> sumMontantByEtat(StatutFacture etat) {
        return repository.sumMontantByEtat(etat);
    }

    @Override
    public Mono<Long> countByStatut(String statut) {
        return repository.countByStatut(statut);
    }

    @Override
    public Mono<BigDecimal> sumMontantByStatut(String statut) {
        return repository.sumMontantByStatut(statut);
    }

    @Override
    public Mono<Long> countByDateBetween(LocalDate startDate, LocalDate endDate) {
        return repository.countByDateBetween(startDate, endDate);
    }

    @Override
    public Mono<Boolean> existsByNumeroFacture(String numeroFacture) {
        return repository.existsByNumeroFacture(numeroFacture);
    }

    @Override
    public Mono<Facture> save(Facture facture) {
        return repository.save(mapper.toEntity(facture)).map(mapper::toDomain);
    }

    @Override
    public Mono<Facture> insert(Facture facture) {
        return entityTemplate.insert(mapper.toEntity(facture)).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Flux<Facture> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return repository.count();
    }
}
