package com.example.account.modules.media.domain.port.output;

import com.example.account.modules.media.dto.CreateMediaAssetRequest;
import com.example.account.modules.media.dto.MediaAssetResponse;
import com.example.account.modules.media.dto.StoredFileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MediaServicePort {
    Mono<StoredFileResponse> uploadFile(FilePart filePart, String documentType);
    Mono<ResponseEntity<byte[]>> getFileContent(UUID fileId);
    Mono<MediaAssetResponse> createMediaAsset(CreateMediaAssetRequest request);
    Flux<MediaAssetResponse> getMediaAssets(String targetType, UUID targetId);
}
