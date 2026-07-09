package com.example.account.modules.media.application.usecase.impl;

import com.example.account.modules.media.domain.port.input.MediaUseCase;
import com.example.account.modules.media.domain.port.output.MediaServicePort;
import com.example.account.modules.media.dto.CreateMediaAssetRequest;
import com.example.account.modules.media.dto.MediaAssetResponse;
import com.example.account.modules.media.dto.StoredFileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaUseCaseImpl implements MediaUseCase {

    private final MediaServicePort mediaServicePort;

    @Override
    public Mono<StoredFileResponse> uploadFile(FilePart filePart, String documentType) {
        return mediaServicePort.uploadFile(filePart, documentType);
    }

    @Override
    public Mono<ResponseEntity<byte[]>> getFileContent(UUID fileId) {
        return mediaServicePort.getFileContent(fileId);
    }

    @Override
    public Mono<MediaAssetResponse> createMediaAsset(CreateMediaAssetRequest request) {
        return mediaServicePort.createMediaAsset(request);
    }

    @Override
    public Flux<MediaAssetResponse> getMediaAssets(String targetType, UUID targetId) {
        return mediaServicePort.getMediaAssets(targetType, targetId);
    }
}
