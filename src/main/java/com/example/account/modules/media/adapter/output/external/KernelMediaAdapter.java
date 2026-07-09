package com.example.account.modules.media.adapter.output.external;

import com.example.account.modules.media.dto.CreateMediaAssetRequest;
import com.example.account.modules.media.dto.MediaAssetResponse;
import com.example.account.modules.media.dto.StoredFileResponse;
import com.example.account.modules.media.domain.port.output.MediaServicePort;
import com.example.account.modules.shared.dto.kernel.KernelApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.UUID;

@Service
@Slf4j
public class KernelMediaAdapter implements MediaServicePort {

    private final WebClient kernelWebClient;

    public KernelMediaAdapter(@Qualifier("kernelWebClient") WebClient kernelWebClient) {
        this.kernelWebClient = kernelWebClient;
    }

    private static final ParameterizedTypeReference<KernelApiResponse<StoredFileResponse>> STORED_FILE_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<MediaAssetResponse>> MEDIA_ASSET_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<KernelApiResponse<List<MediaAssetResponse>>> MEDIA_ASSET_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

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
        return kernelWebClient
                .post()
                .uri("/api/media-assets")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        com.example.account.modules.core.exception.SalesCoreErrorMapper.forContext(
                                "Kernel media-assets error for request " + request))
                .bodyToMono(MEDIA_ASSET_TYPE)
                .map(KernelApiResponse::getData);
    }

    @Override
    public Flux<MediaAssetResponse> getMediaAssets(String targetType, UUID targetId) {
        return kernelWebClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/api/media-assets")
                        .queryParam("targetType", targetType)
                        .queryParam("targetId", targetId)
                        .build())
                .retrieve()
                .bodyToMono(MEDIA_ASSET_LIST_TYPE)
                .flatMapMany(resp -> resp == null || resp.getData() == null ? Flux.empty() : Flux.fromIterable(resp.getData()));
    }
}
