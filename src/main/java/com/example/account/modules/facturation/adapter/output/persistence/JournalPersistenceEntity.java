package com.example.account.modules.facturation.adapter.output.persistence;

import com.example.account.modules.core.adapter.output.persistence.OrganizationScopedPersistenceEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("journals")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalPersistenceEntity extends OrganizationScopedPersistenceEntity {

    @Id
    @Column("id_journal")
    private UUID idJournal;

    @Column("nom_journal")
    private String nomJournal;

    @Column("type")
    private String type;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
