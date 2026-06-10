package com.example.account.modules.facturation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@Slf4j
public class PdfGeneratorService {

    private final WebClient webClient;

    public PdfGeneratorService() {
        // Using Port 8081 as mapped in Docker Compose
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                // Increase buffer to 32MB for larger PDFs with images or QR codes
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(32 * 1024 * 1024))
                .build();
    }

    /**
     * Converts HTML to PDF bytes using Gotenberg Chromium engine.
     * Generates PDFs entirely in memory; no files are created locally.
     *
     * @param html - HTML content to convert
     * @return PDF bytes
     */
    public byte[] generatePdfFromHtml(String html) {
        log.info("Requesting PDF bytes from Gotenberg Chromium engine...");

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("files", new ByteArrayResource(html.getBytes(StandardCharsets.UTF_8)))
                .filename("index.html")
                .contentType(MediaType.TEXT_HTML);

        try {
            // Set a longer timeout (30 seconds) to avoid Chromium websocket timeout
            Mono<byte[]> pdfMono = webClient.post()
                    .uri("/forms/chromium/convert/html")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .doOnSuccess(bytes -> log.info("Successfully received PDF bytes (Size: {} bytes)", bytes.length))
                    .doOnError(err -> log.error("Error generating PDF: {}", err.getMessage()));

            // Use block with timeout to prevent indefinite hanging
            return pdfMono.block(Duration.ofSeconds(30));

        } catch (Exception e) {
            log.error("PDF generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Could not generate PDF via Gotenberg", e);
        }
    }
}