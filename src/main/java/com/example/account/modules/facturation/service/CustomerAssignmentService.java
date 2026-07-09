package com.example.account.modules.facturation.service;

import com.example.account.modules.facturation.dto.request.AssignCustomerRequest;
import com.example.account.modules.facturation.dto.response.CustomerAssignmentResponse;
import com.example.account.modules.facturation.model.entity.CustomerAssignment;
import com.example.account.modules.facturation.repository.CustomerAssignmentRepository;
import com.example.account.modules.tiers.domain.port.input.ClientUseCase;
import com.example.account.modules.tiers.dto.ClientResponse;
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
public class CustomerAssignmentService {

    private final CustomerAssignmentRepository repository;
    private final R2dbcEntityTemplate entityTemplate;
    private final ClientUseCase clientUseCase;

    @Transactional
    public Mono<CustomerAssignmentResponse> assign(AssignCustomerRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return repository.findByClientIdAndOrganizationId(request.getClientId(), request.getOrganizationId())
                .flatMap(existing -> {
                    existing.setSellerId(request.getSellerId());
                    existing.setSellerName(request.getSellerName());
                    existing.setUpdatedAt(now);
                    return repository.save(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    CustomerAssignment assignment = CustomerAssignment.builder()
                            .idAssignment(UUID.randomUUID())
                            .organizationId(request.getOrganizationId())
                            .clientId(request.getClientId())
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
    public Mono<CustomerAssignmentResponse> getForClient(UUID clientId, UUID organizationId) {
        return repository.findByClientIdAndOrganizationId(clientId, organizationId)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<CustomerAssignmentResponse> listByOrganization(UUID organizationId) {
        return repository.findByOrganizationId(organizationId).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<CustomerAssignmentResponse> listBySeller(UUID sellerId, UUID organizationId) {
        return repository.findBySellerIdAndOrganizationId(sellerId, organizationId).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<ClientResponse> getCustomersForSeller(UUID sellerId, UUID organizationId) {
        return repository.findBySellerIdAndOrganizationId(sellerId, organizationId)
                .flatMap(a -> clientUseCase.getClientById(a.getClientId()));
    }

    @Transactional
    public Mono<Void> unassign(UUID clientId, UUID organizationId) {
        return repository.deleteByClientIdAndOrganizationId(clientId, organizationId);
    }

    private CustomerAssignmentResponse toResponse(CustomerAssignment a) {
        return CustomerAssignmentResponse.builder()
                .idAssignment(a.getIdAssignment())
                .organizationId(a.getOrganizationId())
                .clientId(a.getClientId())
                .sellerId(a.getSellerId())
                .sellerName(a.getSellerName())
                .assignedAt(a.getAssignedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
