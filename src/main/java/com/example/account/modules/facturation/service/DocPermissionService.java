package com.example.account.modules.facturation.service;

import com.example.account.modules.facturation.dto.request.AssignDocPermissionRequest;
import com.example.account.modules.facturation.dto.response.DocPermissionResponse;
import com.example.account.modules.facturation.model.entity.DocPermission;
import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;
import com.example.account.modules.facturation.repository.DocPermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocPermissionService {

    private final DocPermissionRepository repository;
    private final R2dbcEntityTemplate entityTemplate;
    private final EmailService emailService;

    @Value("${billing.frontend-url:http://localhost:3000}")
    private String billingFrontendUrl;

    @Transactional
    public Mono<DocPermissionResponse> grant(AssignDocPermissionRequest request) {
        LocalDateTime now = LocalDateTime.now();

        // Ownership is exclusive: granting OWNER to a seller transfers it away
        // from whoever held it before, for this document.
        Mono<Void> revokeOtherOwners = request.getPermission() == DocPermissionLevel.OWNER
                ? repository.deleteByDocIdAndDocTypeAndPermissionAndSellerIdNot(
                        request.getDocId(), request.getDocType(), DocPermissionLevel.OWNER, request.getSellerId())
                : Mono.empty();

        return revokeOtherOwners.then(
                repository.findBySellerIdAndDocIdAndDocType(request.getSellerId(), request.getDocId(), request.getDocType())
                        .flatMap(existing -> {
                            existing.setPermission(request.getPermission());
                            existing.setUpdatedAt(now);
                            return repository.save(existing);
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            DocPermission permission = DocPermission.builder()
                                    .idPermission(UUID.randomUUID())
                                    .sellerId(request.getSellerId())
                                    .docId(request.getDocId())
                                    .docType(request.getDocType())
                                    .permission(request.getPermission())
                                    .assignedAt(now)
                                    .updatedAt(now)
                                    .build();
                            return entityTemplate.insert(permission);
                        }))
        ).map(this::toResponse);
    }

    /**
     * Same as {@link #grant}, but for the explicit "Share" action — also emails
     * the recipient that they've been invited on the document. grant() alone
     * (used silently for the OWNER auto-assignment at document creation) never emails.
     */
    @Transactional
    public Mono<DocPermissionResponse> share(AssignDocPermissionRequest request) {
        return grant(request)
                .flatMap(response -> {
                    if (!StringUtils.hasText(request.getRecipientEmail())) {
                        return Mono.just(response);
                    }
                    return emailService.sendDocPermissionInviteNotification(
                                    request.getRecipientEmail(),
                                    request.getRecipientName(),
                                    request.getSharedByName(),
                                    request.getPermission().name(),
                                    request.getDocLabel(),
                                    billingFrontendUrl + "/login"
                            )
                            .onErrorResume(e -> {
                                log.error("Failed to send doc-permission invite email to {}: {}",
                                        request.getRecipientEmail(), e.getMessage());
                                return Mono.empty();
                            })
                            .thenReturn(response);
                });
    }

    @Transactional(readOnly = true)
    public Mono<DocPermissionResponse> getForSellerAndDoc(UUID sellerId, UUID docId, DocType docType) {
        return repository.findBySellerIdAndDocIdAndDocType(sellerId, docId, docType).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<DocPermissionResponse> listBySeller(UUID sellerId) {
        return repository.findBySellerId(sellerId).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Flux<DocPermissionResponse> listByDoc(UUID docId, DocType docType) {
        return repository.findByDocIdAndDocType(docId, docType).map(this::toResponse);
    }

    /**
     * Raw permission entries (not doc bodies) held by a seller for a given doc type —
     * used by each doc type's service to resolve which documents a seller can access.
     */
    @Transactional(readOnly = true)
    public Flux<DocPermission> findBySellerAndDocType(UUID sellerId, DocType docType) {
        return repository.findBySellerIdAndDocType(sellerId, docType);
    }

    @Transactional
    public Mono<Void> revoke(UUID sellerId, UUID docId, DocType docType) {
        return repository.deleteBySellerIdAndDocIdAndDocType(sellerId, docId, docType);
    }

    @Transactional
    public Mono<Void> deleteByDocIdAndDocType(UUID docId, DocType docType) {
        log.info("Deleting all permission records for document {} ({})", docId, docType);
        return repository.deleteByDocIdAndDocType(docId, docType);
    }

    public DocPermissionResponse toResponse(DocPermission p) {
        return DocPermissionResponse.builder()
                .idPermission(p.getIdPermission())
                .sellerId(p.getSellerId())
                .docId(p.getDocId())
                .docType(p.getDocType())
                .permission(p.getPermission())
                .assignedAt(p.getAssignedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
