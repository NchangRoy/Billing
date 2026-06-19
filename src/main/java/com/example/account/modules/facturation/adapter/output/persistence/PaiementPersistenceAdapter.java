package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.domain.model.Paiement;
import com.example.account.modules.facturation.domain.port.output.PaiementRepositoryPort;
import com.example.account.modules.facturation.adapter.output.persistence.mapper.PaiementPersistenceMapper;
import com.example.account.modules.facturation.model.enums.TypePaiement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaiementPersistenceAdapter implements PaiementRepositoryPort {

    private final PaiementR2dbcRepository repository;
    private final PaiementPersistenceMapper mapper;
    private final org.springframework.data.r2dbc.core.R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<Paiement> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<Paiement> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }

    @Override
    public Flux<Paiement> findByIdClient(UUID idClient) {
        return repository.findByIdClient(idClient).map(mapper::toDomain);
    }

    @Override
    public Flux<Paiement> findByIdFacture(UUID idFacture) {
        return repository.findByIdFacture(idFacture).map(mapper::toDomain);
    }

    @Override
    public Flux<Paiement> findByModePaiement(TypePaiement mode) {
        return repository.findByModePaiement(mode).map(mapper::toDomain);
    }

    @Override
    public Flux<Paiement> findByDateBetween(LocalDate start, LocalDate end) {
        return repository.findByDateBetween(start, end).map(mapper::toDomain);
    }

    @Override
    public Mono<BigDecimal> sumMontantByClient(UUID idClient) {
        return repository.sumMontantByClient(idClient);
    }

    @Override
    public Mono<BigDecimal> sumMontantByFacture(UUID idFacture) {
        return repository.sumMontantByFacture(idFacture);
    }

    @Override
    public Mono<BigDecimal> sumMontantByDateBetween(LocalDate start, LocalDate end) {
        return repository.sumMontantByDateBetween(start, end);
    }

    @Override
    public Mono<Long> countByIdClient(UUID idClient) {
        return repository.countByIdClient(idClient);
    }

    @Override
    public Mono<Long> countByModePaiement(TypePaiement mode) {
        return repository.countByModePaiement(mode);
    }

    @Override
    public Flux<Paiement> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }

    @Override
    public Flux<Paiement> findByAgencyId(UUID agencyId) {
        return repository.findByAgencyId(agencyId).map(mapper::toDomain);
    }

    @Override
    public Mono<Paiement> save(Paiement paiement) {
        PaiementPersistenceEntity entity = mapper.toEntity(paiement);
        if (entity.getIdPaiement() == null) {
            entity.setIdPaiement(UUID.randomUUID());
            return entityTemplate.insert(entity).map(mapper::toDomain);
        }
        return repository.existsById(entity.getIdPaiement())
                .flatMap(exists -> exists ? repository.save(entity) : entityTemplate.insert(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }
}
