package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.dto.request.NoteCreditRequest;
import com.example.account.modules.facturation.dto.response.NoteCreditResponse;
import com.example.account.modules.facturation.domain.port.input.NoteCreditUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facturation/note-credits")
@RequiredArgsConstructor
public class NoteCreditController {

    private final NoteCreditUseCase noteCreditService;

    @PostMapping
    public Mono<ResponseEntity<NoteCreditResponse>> createNoteCredit(@RequestBody NoteCreditRequest request) {
        return noteCreditService.createNoteCredit(request)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<NoteCreditResponse>> updateNoteCredit(@PathVariable UUID id, @RequestBody NoteCreditRequest request) {
        return noteCreditService.updateNoteCredit(id, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<NoteCreditResponse>> getNoteCreditById(@PathVariable UUID id) {
        return noteCreditService.getNoteCreditById(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<NoteCreditResponse> getAllNoteCredits() {
        return noteCreditService.getAllNoteCredits();
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteNoteCredit(@PathVariable UUID id) {
        return noteCreditService.deleteNoteCredit(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
