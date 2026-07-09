package com.example.account.modules.settings.repository;

import com.example.account.modules.facturation.model.enums.TypeNumerotation;
import com.example.account.modules.settings.domain.Setting;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface SettingRepository extends R2dbcRepository<Setting, Long> {
    Flux<Setting> findByOrganizationId(UUID organizationId);
    Mono<Setting> findByOrganizationIdAndTypeNumerotation(UUID organizationId, TypeNumerotation typeNumerotation);
    Mono<Setting> findByOrganizationIdAndTypeNumerotationIsNull(UUID organizationId);
}
