package com.example.account.modules.facturation.model.entity.Others;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import com.example.account.modules.facturation.service.ExternalServices.entity.enums.ResourceType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("portal_access_tokens")
public class PortalAccessToken {

    @Id
    @Column("token_id")
    private Long tokenId;

    @Column("token")
    private String token;

    @Column("resource_type")
    private ResourceType resourceType;

    @Column("resource_id")
    private UUID resourceId;

    @Column("client_email")
    private String clientEmail;

    @Column("can_view")
    private Boolean canView;

    @Column("can_modify")
    private Boolean canModify;

    @Column("can_accept")
    private Boolean canAccept;

    @Column("can_reject")
    private Boolean canReject;

    @Column("expires_at")
    private LocalDateTime expiresAt;

    @Column("used")
    @Builder.Default
    private boolean used = false;

    @Column("used_at")
    private LocalDateTime usedAt;

    @Column("created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}