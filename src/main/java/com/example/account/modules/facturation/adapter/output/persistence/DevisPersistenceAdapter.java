package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.adapter.output.persistence.mapper.DevisPersistenceMapper;
import com.example.account.modules.facturation.domain.model.Devis;
import com.example.account.modules.facturation.domain.port.output.DevisRepositoryPort;
import com.example.account.modules.facturation.model.enums.StatutDevis;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class DevisPersistenceAdapter implements DevisRepositoryPort {

    private final DevisR2dbcRepository repository;
    private final DevisPersistenceMapper mapper;
    private final R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<Devis> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<Devis> findByNumeroDevis(String numeroDevis) {
        return repository.findByNumeroDevis(numeroDevis).map(mapper::toDomain);
    }

    @Override
    public Flux<Devis> findByStatut(StatutDevis statut) {
        return repository.findByStatut(statut).map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNumeroDevis(String numeroDevis) {
        return repository.existsByNumeroDevis(numeroDevis);
    }

    @Override
    public Flux<Devis> findByIdClient(UUID idClient) {
        return repository.findByIdClient(idClient).map(mapper::toDomain);
    }

    @Override
    public Flux<Devis> findExpiredDevis(LocalDate date) {
        return repository.findExpiredDevis(date).map(mapper::toDomain);
    }

    @Override
    public Flux<Devis> findByDateCreationBetween(LocalDate start, LocalDate end) {
        return repository.findByDateCreationBetween(start, end).map(mapper::toDomain);
    }

    @Override
    public Flux<Devis> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(mapper::toDomain);
    }

    @Override
    public Flux<Devis> findByAgencyId(UUID agencyId) {
        return repository.findByAgencyId(agencyId).map(mapper::toDomain);
    }

    @Override
    public Mono<Devis> save(Devis devis) {
        // Special case to handle insert vs update using entityTemplate like old service did if needed,
        // but typically repo.save() works if id is present and it's new, wait, R2DBC can be tricky.
        // The old service used entityTemplate.insert(devis) for new and repo.save(devis) for update.
        // Let's abstract that inside here if idDevis is null or we can just use entityTemplate for insert.
        return repository.save(mapper.toEntity(devis)).map(mapper::toDomain);
    }
    
    public Mono<Devis> insert(Devis devis) {
        return entityTemplate.insert(mapper.toEntity(devis)).map(mapper::toDomain);
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
    public Flux<Devis> findAll() {
        return repository.findAll().map(mapper::toDomain);
    }
}
