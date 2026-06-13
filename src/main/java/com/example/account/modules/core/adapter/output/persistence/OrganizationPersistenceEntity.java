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
import java.time.LocalTime;
import java.util.UUID;

@Table("organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationPersistenceEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("code")
    private String code;

    @Column("short_name")
    private String shortName;

    @Column("long_name")
    private String longName;

    @Column("description")
    private String description;

    @Column("logo")
    private String logo;

    @Column("is_active")
    private Boolean isActive = true;

    @Column("is_individual")
    private Boolean isIndividual = false;

    @Column("is_public")
    private Boolean isPublic = false;

    @Column("is_business")
    private Boolean isBusiness = true;

    @Column("country")
    private String country;

    @Column("city")
    private String city;

    @Column("address")
    private String address;

    @Column("localization")
    private String localization;

    @Column("open_time")
    private LocalTime openTime;

    @Column("close_time")
    private LocalTime closeTime;

    @Column("email")
    private String email;

    @Column("phone")
    private String phone;

    @Column("whatsapp")
    private String whatsapp;

    @Column("social_networks")
    private String socialNetworks;

    @Column("capital_share")
    private Double capitalShare;

    @Column("tax_number")
    private String taxNumber;

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
