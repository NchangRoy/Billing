package com.example.account.modules.facturation.domain.port.output;

import com.example.account.modules.facturation.domain.model.Devis;
import com.example.account.modules.facturation.model.enums.StatutDevis;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public interface DevisRepositoryPort {
    Mono<Devis> findById(UUID id);
    Mono<Devis> findByNumeroDevis(String numeroDevis);
    Flux<Devis> findByStatut(StatutDevis statut);
    Mono<Boolean> existsByNumeroDevis(String numeroDevis);
    Flux<Devis> findByIdClient(UUID idClient);
    Flux<Devis> findExpiredDevis(LocalDate date);
    Flux<Devis> findByDateCreationBetween(LocalDate start, LocalDate end);
    Flux<Devis> findByOrganizationId(UUID organizationId);
    Flux<Devis> findByAgencyId(UUID agencyId);
    Mono<Devis> save(Devis devis);
    Mono<Devis> insert(Devis devis);
    Mono<Void> deleteById(UUID id);
    Mono<Boolean> existsById(UUID id);
    Flux<Devis> findAll();
}
