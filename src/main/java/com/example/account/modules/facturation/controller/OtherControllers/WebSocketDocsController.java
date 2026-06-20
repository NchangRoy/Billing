package com.example.account.modules.facturation.controller.OtherControllers;

import com.example.account.modules.facturation.dto.request.ExternalRequest.ReserveRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Documentation-only controller. These are NOT callable REST endpoints.
 * Connect via STOMP WebSocket at ws://host/ws and send to the destinations below.
 */
@RestController
@RequestMapping("/docs/websocket")
@Tag(
    name = "WebSocket (STOMP) — Documentation Only",
    description = """
        These endpoints are NOT REST — they document the WebSocket (STOMP) contract.

        **Connect:** `ws://<host>/ws`
        **Subscribe for live stock updates:** `/topic/stock`

        Use a STOMP client (SockJS + stomp.js on the frontend).
        """
)
public class WebSocketDocsController {

    @PostMapping("/stock.reserve")
    @Operation(
        summary = "Reserve or cancel a product reservation",
        description = """
            **STOMP destination:** `/app/stock.reserve`

            Send a `ReserveRequest` payload. Use `action: RESERVE` to hold stock \
            or `action: CANCEL` to release it.

            After processing, all clients subscribed to `/topic/stock` receive \
            the updated `ProductResponse` automatically.
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                schema = @Schema(implementation = ReserveRequest.class),
                examples = {
                    @ExampleObject(name = "Reserve", value = """
                        {
                          "productId": "11111111-0000-0000-0000-000000000001",
                          "sellerId":  "aaaaaaaa-0000-0000-0000-000000000001",
                          "organizationId": "00000000-0000-0000-0000-000000000001",
                          "quantity": 2,
                          "action": "RESERVE"
                        }"""),
                    @ExampleObject(name = "Cancel", value = """
                        {
                          "productId": "11111111-0000-0000-0000-000000000001",
                          "sellerId":  "aaaaaaaa-0000-0000-0000-000000000001",
                          "organizationId": "00000000-0000-0000-0000-000000000001",
                          "quantity": 2,
                          "action": "CANCEL"
                        }""")
                }
            )
        ),
        responses = @ApiResponse(
            responseCode = "200",
            description = "Broadcast to /topic/stock after update",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
        )
    )
    public void stockReserve(@RequestBody ReserveRequest request) {
        throw new UnsupportedOperationException("WebSocket only — use STOMP at /app/stock.reserve");
    }
}
