package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.AssignCustomerRequest;
import com.example.account.modules.facturation.dto.response.CustomerAssignmentResponse;
import com.example.account.modules.facturation.service.CustomerAssignmentService;
import com.example.account.modules.tiers.dto.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/customer-assignments")
@RequiredArgsConstructor
@Tag(name = "Customer Assignments", description = "Assigning customers to their account-manager seller")
public class CustomerAssignmentController {

    private final CustomerAssignmentService service;

    @PostMapping
    @Operation(summary = "Assign (or reassign) a customer to a seller")
    public Mono<CustomerAssignmentResponse> assign(@Valid @RequestBody AssignCustomerRequest request) {
        return service.assign(request);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get the current assignment for a customer")
    public Mono<CustomerAssignmentResponse> getForClient(@PathVariable UUID clientId, @RequestParam UUID organizationId) {
        return service.getForClient(clientId, organizationId);
    }

    @GetMapping("/organisation/{organizationId}")
    @Operation(summary = "List all customer assignments for an organization")
    public Flux<CustomerAssignmentResponse> listByOrganization(@PathVariable UUID organizationId) {
        return service.listByOrganization(organizationId);
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "List all customer assignments for a seller")
    public Flux<CustomerAssignmentResponse> listBySeller(@PathVariable UUID sellerId, @RequestParam UUID organizationId) {
        return service.listBySeller(sellerId, organizationId);
    }

    @GetMapping("/seller/{sellerId}/customers")
    @Operation(summary = "Get the customers assigned to a seller")
    public Flux<ClientResponse> getCustomersForSeller(@PathVariable UUID sellerId, @RequestParam UUID organizationId) {
        return service.getCustomersForSeller(sellerId, organizationId);
    }

    @DeleteMapping("/client/{clientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a customer's seller assignment")
    public Mono<Void> unassign(@PathVariable UUID clientId, @RequestParam UUID organizationId) {
        return service.unassign(clientId, organizationId);
    }
}
