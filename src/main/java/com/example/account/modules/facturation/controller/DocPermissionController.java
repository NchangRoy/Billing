package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.AssignDocPermissionRequest;
import com.example.account.modules.facturation.dto.response.DocPermissionResponse;
import com.example.account.modules.facturation.model.enums.DocType;
import com.example.account.modules.facturation.service.DocPermissionService;
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
@RequestMapping("/api/doc-permissions")
@RequiredArgsConstructor
@Tag(name = "Document Permissions", description = "Per-document ACL grants (owner/editor/viewer) for sellers")
public class DocPermissionController {

    private final DocPermissionService service;

    @PostMapping
    @Operation(summary = "Grant (or update) a seller's permission on a document")
    public Mono<DocPermissionResponse> grant(@Valid @RequestBody AssignDocPermissionRequest request) {
        return service.grant(request);
    }

    @PostMapping("/share")
    @Operation(summary = "Share a document with a seller at the given permission level, emailing them a notification. " +
            "Sharing as OWNER transfers ownership away from whoever held it before.")
    public Mono<DocPermissionResponse> share(@Valid @RequestBody AssignDocPermissionRequest request) {
        return service.share(request);
    }

    @GetMapping("/seller/{sellerId}/doc/{docId}")
    @Operation(summary = "Get a seller's permission on a specific document")
    public Mono<DocPermissionResponse> getForSellerAndDoc(
            @PathVariable UUID sellerId,
            @PathVariable UUID docId,
            @RequestParam DocType docType) {
        return service.getForSellerAndDoc(sellerId, docId, docType);
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "List all document permissions granted to a seller")
    public Flux<DocPermissionResponse> listBySeller(@PathVariable UUID sellerId) {
        return service.listBySeller(sellerId);
    }

    @GetMapping("/doc/{docId}")
    @Operation(summary = "List all sellers with a permission on a document")
    public Flux<DocPermissionResponse> listByDoc(@PathVariable UUID docId, @RequestParam DocType docType) {
        return service.listByDoc(docId, docType);
    }

    @DeleteMapping("/seller/{sellerId}/doc/{docId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoke a seller's permission on a document")
    public Mono<Void> revoke(
            @PathVariable UUID sellerId,
            @PathVariable UUID docId,
            @RequestParam DocType docType) {
        return service.revoke(sellerId, docId, docType);
    }
}
