package com.example.account.modules.media.adapter.output.external;

import com.example.account.modules.media.dto.CreateMediaAssetRequest;
import com.example.account.modules.media.dto.MediaAssetResponse;
import com.example.account.modules.media.dto.StoredFileResponse;
import com.example.account.modules.media.domain.port.output.MediaServicePort;
import com.example.account.modules.shared.dto.kernel.KernelApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Kernel has no generic cross-entity "media asset" registry — the only
 * media endpoints it exposes are product-scoped:
 * POST/GET /api/product-core/products/{productId}/media (body: mediaType/uri/sortOrder,
 * not fileId). Billing's own CreateMediaAssetRequest/MediaAssetResponse keep their
 * generic targetType/targetId/fileId shape for the frontend — this adapter is where
 * that shape gets translated to and from Kernel's real one. targetType is accepted
 * but unused beyond passthrough since every caller already sends "PRODUCT" (Kernel
 * has nothing else to target); targetId is always the productId.
 */
@Service
@Slf4j
public class KernelMediaAdapter implements MediaServicePort {

    private final WebClient kernelWebClient;

    @Value("${app.base-url}")
    private String appBaseUrl;

    public KernelMediaAdapter(@Qualifier("kernelWebClient") WebClient kernelWebClient) {
        this.kernelWebClient = kernelWebClient;
    }

    private static final ParameterizedTypeReference<KernelApiResponse<StoredFileResponse>> STORED_FILE_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<KernelMediaView>> MEDIA_VIEW_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<List<KernelMediaView>>> MEDIA_VIEW_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    /** Mirrors Kernel's MediaView schema (/v3/api-docs) — id/productId/mediaType/uri/altText/sortOrder/primary. */
    private record KernelMediaView(UUID id, UUID productId, String mediaType, String uri, String altText,
                                    Integer sortOrder, Boolean primary) {
    }

    @Override
    public Mono<StoredFileResponse> uploadFile(FilePart filePart, String documentType) {
        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(dataBuffer.readableByteCount());
                    dataBuffer.toByteBuffer(byteBuffer);
                    DataBufferUtils.release(dataBuffer);
                    return byteBuffer.array();
                })
                .flatMap(bytes -> {
                    MultipartBodyBuilder builder = new MultipartBodyBuilder();
                    builder.part("file", new ByteArrayResource(bytes) {
                        @Override
                        public String getFilename() {
                            return filePart.filename();
                        }
                    }).contentType(resolveMediaType(filePart));

                    return kernelWebClient
                            .post()
                            .uri(uriBuilder -> uriBuilder.path("/api/files")
                                    .queryParamIfPresent("documentType", java.util.Optional.ofNullable(documentType))
                                    .build())
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .body(org.springframework.web.reactive.function.BodyInserters.fromMultipartData(builder.build()))
                            .retrieve()
                            .bodyToMono(STORED_FILE_TYPE)
                            .map(KernelApiResponse::getData);
                });
    }

    private MediaType resolveMediaType(FilePart filePart) {
        MediaType contentType = filePart.headers().getContentType();
        return contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM;
    }

    @Override
    public Mono<ResponseEntity<byte[]>> getFileContent(UUID fileId) {
        return kernelWebClient
                .get()
                .uri("/api/files/{fileId}", fileId)
                .retrieve()
                .toEntity(byte[].class)
                .map(kernelResponse -> {
                    HttpHeaders headers = new HttpHeaders();
                    MediaType contentType = kernelResponse.getHeaders().getContentType();
                    if (contentType != null) {
                        headers.setContentType(contentType);
                    }
                    return new ResponseEntity<>(kernelResponse.getBody(), headers, kernelResponse.getStatusCode());
                });
    }

    @Override
    public Mono<MediaAssetResponse> createMediaAsset(CreateMediaAssetRequest request) {
        Map<String, Object> body = Map.of(
                "mediaType", toKernelMediaType(request.getMimeType()),
                "uri", fileUri(request.getFileId()),
                "sortOrder", request.getPosition() == null ? 0 : request.getPosition());

        return kernelWebClient
                .post()
                .uri("/api/product-core/products/{productId}/media", request.getTargetId())
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        com.example.account.modules.core.exception.SalesCoreErrorMapper.forContext(
                                "Kernel product media error for request " + request))
                .bodyToMono(MEDIA_VIEW_TYPE)
                .map(KernelApiResponse::getData)
                .map(view -> toMediaAssetResponse(view, request.getTargetType()));
    }

    @Override
    public Flux<MediaAssetResponse> getMediaAssets(String targetType, UUID targetId) {
        return kernelWebClient
                .get()
                .uri("/api/product-core/products/{productId}/media", targetId)
                .retrieve()
                .bodyToMono(MEDIA_VIEW_LIST_TYPE)
                .map(KernelApiResponse::getData)
                .flatMapMany(data -> data == null ? Flux.empty() : Flux.fromIterable(data))
                .map(view -> toMediaAssetResponse(view, targetType));
    }

    private String fileUri(UUID fileId) {
        return appBaseUrl + "/api/media/files/" + fileId;
    }

    private String toKernelMediaType(String mimeType) {
        if (mimeType == null) return "OTHER";
        if (mimeType.startsWith("image/")) return "IMAGE";
        if (mimeType.startsWith("video/")) return "VIDEO";
        if (mimeType.startsWith("audio/")) return "AUDIO";
        return "OTHER";
    }

    /** Kernel's media view has no fileId of its own — it's the trailing segment of the
     * uri we built it from (appBaseUrl + "/api/media/files/{fileId}"). */
    private UUID extractFileId(String uri) {
        if (uri == null) return null;
        try {
            String lastSegment = uri.substring(uri.lastIndexOf('/') + 1);
            return UUID.fromString(lastSegment);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private MediaAssetResponse toMediaAssetResponse(KernelMediaView view, String targetType) {
        MediaAssetResponse response = new MediaAssetResponse();
        response.setId(view.id());
        response.setTargetType(targetType);
        response.setTargetId(view.productId());
        response.setFileId(extractFileId(view.uri()));
        response.setMimeType(view.mediaType());
        response.setPosition(view.sortOrder());
        response.setAltText(view.altText());
        return response;
    }
}
