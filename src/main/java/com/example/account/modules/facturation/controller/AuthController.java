package com.example.account.modules.facturation.controller;

import com.example.account.modules.facturation.domain.port.input.AuthUseCase;
import com.example.account.modules.facturation.dto.request.LoginRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.SellerAuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "Seller authentication")
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/login")
    @Operation(summary = "Seller login", description = "Returns full seller profile with org and agency details on success.")
    public Mono<ResponseEntity<SellerAuthResponse>> login(@RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUsername());
        return authUseCase.login(request)
                .map(ResponseEntity::ok);
    }
}
