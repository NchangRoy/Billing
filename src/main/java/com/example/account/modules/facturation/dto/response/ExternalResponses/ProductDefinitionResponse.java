package com.example.account.modules.facturation.dto.response.ExternalResponses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** An entry from the organization's catalog of defined products — used to populate the
 * "pick an existing product" search when provisioning a new sellable product; it carries no
 * price/cost/stock, which only exists once something is actually provisioned as sellable. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDefinitionResponse {
    private UUID id;
    private String globalCode;
    private String name;
    private String shortDescription;
    private String longDescription;
    private String nature;
    private String articleKind;
    private String lifecycleStatus;
    private UUID defaultUomId;
    private UUID categoryId;
    private String launchDate;
    private String discontinueDate;
}
