package com.example.account.modules.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    private UUID id;
    private String code;
    private String shortName;
    private String longName;
    private String description;
    private String logo;
    private Boolean isActive = true;
    private Boolean isIndividual = false;
    private Boolean isPublic = false;
    private Boolean isBusiness = true;
    private String country;
    private String city;
    private String address;
    private String localization;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String email;
    private String phone;
    private String whatsapp;
    private String socialNetworks;
    private Double capitalShare;
    private String taxNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
