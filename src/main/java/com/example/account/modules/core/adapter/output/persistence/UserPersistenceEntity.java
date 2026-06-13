package com.example.account.modules.core.adapter.output.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPersistenceEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("username")
    private String username;

    @Column("email")
    private String email;

    @Column("password")
    private String password;

    @Column("is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("deleted_at")
    private LocalDateTime deletedAt;

    @Transient
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
