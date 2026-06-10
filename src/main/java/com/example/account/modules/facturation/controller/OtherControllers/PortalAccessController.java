package com.example.account.modules.facturation.controller.OtherControllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.modules.facturation.dto.response.DevisResponse;
import com.example.account.modules.facturation.dto.response.ExternalResponses.PortalAcessResponse;
import com.example.account.modules.facturation.service.ExternalServices.PortalAccessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/portal-access")
public class PortalAccessController {
    @Autowired
    private PortalAccessService portalAccessService;

    @Operation(summary = "Get Quotation", description = "Enter the long token string to fetch data")
    @GetMapping("/quotation/{token}")
    public Mono<ResponseEntity<PortalAcessResponse<DevisResponse>>> getQuotationInfo(
        @Parameter(name = "token", description = "The generated UUID string", required = true) 
        @PathVariable String token
    ) {
        return portalAccessService.getDevisInformation(token)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
  @GetMapping("/{token}")
public Mono<ResponseEntity<Void>> handleAction(
    @Parameter(name = "token", description = "The generated UUID string", required = true) 
    @PathVariable String token,
    @Parameter(name = "action", description = "Action Submitted by Customer")
    @RequestParam String action
) {
    return portalAccessService.handleAction(token, action)
        // Since handleAction returns Mono<Void>, .map() won't work.
        // .thenReturn() waits for completion and then returns the OK response.
        .thenReturn(ResponseEntity.ok().<Void>build())
        // If the service returns Mono.empty() unexpectedly
        .defaultIfEmpty(ResponseEntity.notFound().build())
        // Proper error handling for expired tokens or invalid actions
        .onErrorResume(e -> {
            log.error("Error processing portal action: {}", e.getMessage());
            return Mono.just(ResponseEntity.badRequest().build());
        });
}

}