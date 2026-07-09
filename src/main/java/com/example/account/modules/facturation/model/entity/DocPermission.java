package com.example.account.modules.facturation.model.entity;

import com.example.account.modules.facturation.model.enums.DocPermissionLevel;
import com.example.account.modules.facturation.model.enums.DocType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ACL entry granting a seller a permission level on a specific document.
 * One active grant per (sellerId, docId, docType) — granting again just replaces it.
 */
@Table("doc_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocPermission {

    @Id
    @Column("id_permission")
    private UUID idPermission;

    @Column("seller_id")
    private UUID sellerId;

    @Column("doc_id")
    private UUID docId;

    @Column("doc_type")
    private DocType docType;

    @Column("permission")
    private DocPermissionLevel permission;

    @Column("assigned_at")
    private LocalDateTime assignedAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
