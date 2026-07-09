package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.service.PdfGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Generic HTML-to-PDF rendering. The frontend already builds a full HTML
 * string per document for the print flow (generateFactureHTML, etc.) — this
 * lets the same HTML be turned into a downloadable PDF without needing a
 * dedicated endpoint (or duplicated HTML-building logic) per document type.
 */
@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
@Tag(name = "Pdf", description = "Generic HTML-to-PDF rendering shared by every document's download flow")
public class PdfController {

    private final PdfGeneratorService pdfGeneratorService;

    @Data
    public static class RenderPdfRequest {
        @NotBlank
        private String html;
        private String filename;
    }

    @PostMapping("/render")
    @Operation(summary = "Render HTML (built client-side for print) into a downloadable PDF")
    public Mono<ResponseEntity<byte[]>> render(@Valid @RequestBody RenderPdfRequest request) {
        return Mono.fromCallable(() -> pdfGeneratorService.generatePdfFromHtml(request.getHtml()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + sanitizeFilename(request.getFilename()) + ".pdf\"")
                        .body(bytes));
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) return "document";
        return filename.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}
