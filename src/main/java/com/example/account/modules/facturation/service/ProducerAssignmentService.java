package com.example.account.modules.facturation.service;

import com.example.account.modules.facturation.dto.request.AssignProducerRequest;
import com.example.account.modules.facturation.dto.response.ProducerAssignmentResponse;
import com.example.account.modules.facturation.model.entity.ProducerAssignment;
import com.example.account.modules.facturation.repository.ProducerAssignmentRepository;
import com.example.account.modules.tiers.domain.port.input.FournisseurUseCase;
import com.example.account.modules.tiers.dto.FournisseurResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProducerAssignmentService {

    private final ProducerAssignmentRepository repository;
    private final R2dbcEntityTemplate entityTemplate;
    private final FournisseurUseCase fournisseurUseCase;

    @Transactional
    public Mono<ProducerAssignmentResponse> assign(AssignProducerRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return repository.findByFournisseurIdAndOrganizationId(request.getFournisseurId(), request.getOrganizationId())
                .flatMap(existing -> {
                    existing.setSellerId(request.getSellerId());
                    existing.setSellerName(request.getSellerName());
                    existing.setUpdatedAt(now);
                    return repository.save(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    ProducerAssignment assignment = ProducerAssignment.builder()
                            .idAssignment(UUID.randomUUID())
                            .organizationId(request.getOrganizationId())
                            .fournisseurId(request.getFournisseurId())
                            .sellerId(request.getSellerId())
                            .sellerName(request.getSellerName())
                            .assignedAt(now)
                            .updatedAt(now)
                            .build();
                    return entityTemplate.insert(assignment);
                }))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Mono<ProducerAssignmentResponse> getForFournisseur(UUID fournisseurId, UUID organizationId) {
        return repository.findByFournisseurIdAndOrganizationId(fournisseurId, organizationId)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<ProducerAssignmentResponse> listByOrganization(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<ProducerAssignmentResponse> listBySeller(UUID sellerId, UUID organizationId) {
        return repository.findBySellerIdAndOrganizationId(sellerId, organizationId).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<FournisseurResponse> getProducersForSeller(UUID sellerId, UUID organizationId) {
        return repository.findBySellerIdAndOrganizationId(sellerId, organizationId)
                .flatMap(a -> fournisseurUseCase.getFournisseurById(a.getFournisseurId()));
    }

    @Transactional
    public Mono<Void> unassign(UUID fournisseurId, UUID organizationId) {
        return repository.deleteByFournisseurIdAndOrganizationId(fournisseurId, organizationId);
    }

    private ProducerAssignmentResponse toResponse(ProducerAssignment a) {
        return ProducerAssignmentResponse.builder()
                .idAssignment(a.getIdAssignment())
                .organizationId(a.getOrganizationId())
                .fournisseurId(a.getFournisseurId())
                .sellerId(a.getSellerId())
                .sellerName(a.getSellerName())
                .assignedAt(a.getAssignedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
