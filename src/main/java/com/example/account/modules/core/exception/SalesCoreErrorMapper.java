package com.example.account.modules.core.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * sales-core's error responses embed a full stack trace in "message" (dev
 * profile behavior), so forwarding that string as-is turns every failed
 * request into an unreadable wall of text for the caller (and, worse, the
 * end user if it reaches a toast). This pulls out just the top-level
 * "message" field sales-core itself set, e.g. "Sales point ... already has
 * an open session: ...", and falls back to the raw body only if it isn't
 * the expected JSON shape.
 */
public final class SalesCoreErrorMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SalesCoreErrorMapper() {
    }

    public static Function<ClientResponse, Mono<? extends Throwable>> forContext(String context) {
        return resp -> resp.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new ResponseStatusException(resp.statusCode(), context + ": " + extractMessage(body))));
    }

    public static String extractMessage(String body) {
        try {
            JsonNode node = MAPPER.readTree(body);
            JsonNode message = node.get("message");
            if (message != null && message.isTextual()) {
                return message.asText();
            }
        } catch (Exception ignored) {
            // Not JSON, or no "message" field — fall through to the raw body.
        }
        return body;
    }
}
