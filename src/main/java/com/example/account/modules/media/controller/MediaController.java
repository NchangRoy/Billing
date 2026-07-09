package com.example.account.modules.media.controller;

import com.example.account.modules.media.domain.port.input.MediaUseCase;
import com.example.account.modules.media.dto.CreateMediaAssetRequest;
import com.example.account.modules.media.dto.MediaAssetResponse;
import com.example.account.modules.media.dto.StoredFileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Media", description = "File upload/retrieval and media-asset linking, backed by Kernel's file storage")
public class MediaController {

    private final MediaUseCase mediaUseCase;

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file to Kernel's file storage")
    public Mono<StoredFileResponse> uploadFile(
            @RequestPart("file") FilePart file,
            @RequestParam(required = false) String documentType) {
        return mediaUseCase.uploadFile(file, documentType);
    }

    @GetMapping("/files/{fileId}")
    @Operation(summary = "Fetch a stored file's raw content (e.g. for use as an <img> src)")
    public Mono<ResponseEntity<byte[]>> getFileContent(@PathVariable UUID fileId) {
        return mediaUseCase.getFileContent(fileId);
    }

    @PostMapping("/assets")
    @Operation(summary = "Link an uploaded file to a target (e.g. a product) as a media asset")
    public Mono<MediaAssetResponse> createMediaAsset(@Valid @RequestBody CreateMediaAssetRequest request) {
        return mediaUseCase.createMediaAsset(request);
    }

    @GetMapping("/assets")
    @Operation(summary = "List media assets for a target")
    public Flux<MediaAssetResponse> getMediaAssets(
            @RequestParam String targetType,
            @RequestParam UUID targetId) {
        return mediaUseCase.getMediaAssets(targetType, targetId);
    }
}
