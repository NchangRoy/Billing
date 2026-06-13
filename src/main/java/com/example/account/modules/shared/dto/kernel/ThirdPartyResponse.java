package com.example.account.modules.shared.dto.kernel;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ThirdPartyResponse {
    private UUID id;
    private UUID tenantId;
    private UUID organizationId;
    private String partyType;
    private UUID partyId;
    private String code;
    private Boolean enabled;
    private String referenceCode;
    private String displayName;
    private Set<String> roles;
    private Boolean prospect;
    private String accountingAccount;
    private String segment;
    private Integer qualificationScore;
    private String followUpStatus;
    private Boolean active;
    private String type;
    private String legalForm;
    private String uniqueIdentificationNumber;
    private String tradeRegistrationNumber;
    private String name;
    private String acronym;
    private String longName;
    private String logoUri;
    private UUID logoId;
    private List<String> accountingAccountNumbers;
    private List<String> authorizedPaymentMethods;
    private Double authorizedCreditLimit;
    private Double maxDiscountRate;
    private Boolean vatSubject;
    private Double operationsBalance;
    private Double openingBalance;
    private Integer payTermNumber;
    private String payTermType;
    private String thirdPartyFamily;
    private String classification;
    private String taxNumber;
    private Integer loyaltyPoints;
    private Integer loyaltyPointsUsed;
    private Integer loyaltyPointsExpired;
}
