package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.facturation.model.enums.SubscriptionType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPersistenceEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("organization_id")
    private UUID organizationId;

    @Column("type")
    private SubscriptionType type;

    @Column("started_at")
    private LocalDateTime startedAt;

    @Column("duration_days")
    private int durationDays;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
