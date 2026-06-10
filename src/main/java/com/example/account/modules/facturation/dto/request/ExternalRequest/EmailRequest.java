package com.example.account.modules.facturation.dto.request.ExternalRequest;

import java.util.UUID;

import com.example.account.modules.facturation.model.enums.EmailType;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor

public class EmailRequest {
       
        private String htmlContent;
        private UUID id;
        private String organizationRaisonSociale;
        private Boolean canView;
        private Boolean canAccept;
        private Boolean canReject;
        private Boolean canModify;
}
