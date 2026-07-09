package com.example.account.modules.settings.controller;

import com.example.account.modules.facturation.model.enums.TypeNumerotation;
import com.example.account.modules.settings.dto.SettingResponse;
import com.example.account.modules.settings.dto.UpdateLogoRequest;
import com.example.account.modules.settings.dto.UpdateSequenceSettingRequest;
import com.example.account.modules.settings.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Settings", description = "Organization settings: document numbering composition and company logo")
public class SettingController {

    private final SettingService settingService;

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get organization-wide settings (logo)")
    public Mono<SettingResponse> getOrganizationSettings(@PathVariable UUID organizationId) {
        return settingService.getOrganizationSettings(organizationId);
    }

    @PutMapping("/organization/{organizationId}/logo")
    @Operation(summary = "Set the organization's logo URL")
    public Mono<SettingResponse> updateLogo(@PathVariable UUID organizationId, @Valid @RequestBody UpdateLogoRequest request) {
        return settingService.updateLogo(organizationId, request.getUri());
    }

    @GetMapping("/organization/{organizationId}/numbering")
    @Operation(summary = "List document numbering configuration for every document type")
    public Flux<SettingResponse> listSequenceSettings(@PathVariable UUID organizationId) {
        return settingService.listSequenceSettings(organizationId);
    }

    @PutMapping("/organization/{organizationId}/numbering/{typeNumerotation}")
    @Operation(summary = "Update document numbering configuration for a document type")
    public Mono<SettingResponse> updateSequenceSetting(
            @PathVariable UUID organizationId,
            @PathVariable TypeNumerotation typeNumerotation,
            @Valid @RequestBody UpdateSequenceSettingRequest request) {
        return settingService.updateSequenceSetting(organizationId, typeNumerotation, request);
    }
}
