package com.example.account.modules.facturation.service.ExternalServices;

import com.example.account.modules.facturation.dto.request.ExternalRequest.ReserveRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StockWebSocketController {

    private final ProductExternalService productExternalService;

    // Client sends to /app/stock.reserve
    // Updates are broadcast to /topic/stock by StockBroadcaster after the DB update
    @MessageMapping("/stock.reserve")
    public Mono<Void> reserve(ReserveRequest request) {
        log.info("WebSocket reservation request: action={} product={} seller={} qty={}",
                request.getAction(), request.getProductId(), request.getSellerId(), request.getQuantity());

        return switch (request.getAction()) {
            case RESERVE -> productExternalService.reserveProduct(request);
            case CANCEL -> productExternalService.releaseProduct(request.getProductId(), request.getQuantity());
        };
    }
}
