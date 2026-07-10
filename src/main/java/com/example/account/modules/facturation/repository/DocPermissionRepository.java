package com.example.account.modules.facturation.repository;

import com.example.account.modules.facturation.model.entity.DocPermission;
import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface DocPermissionRepository extends R2dbcRepository<DocPermission, UUID> {
    Mono<DocPermission> findBySellerIdAndDocIdAndDocType(UUID sellerId, UUID docId, DocType docType);
    Flux<DocPermission> findBySellerId(UUID sellerId);
    Flux<DocPermission> findBySellerIdAndDocType(UUID sellerId, DocType docType);
    Flux<DocPermission> findByDocIdAndDocType(UUID docId, DocType docType);
    Mono<Void> deleteBySellerIdAndDocIdAndDocType(UUID sellerId, UUID docId, DocType docType);
    Mono<Void> deleteByDocIdAndDocTypeAndPermissionAndSellerIdNot(UUID docId, DocType docType, DocPermissionLevel permission, UUID sellerId);
    Mono<Void> deleteByDocIdAndDocType(UUID docId, DocType docType);
}
