package com.example.account.modules.facturation.controller.OtherControllers;

import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import com.example.account.modules.facturation.service.ExternalServices.ProductExternalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/webhooks/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Called by ComOps when product data changes")
public class ProductWebhookController {

    private final ProductExternalService productExternalService;

    @Value("${webhook.secret}")
    private String webhookSecret;

    /**
     * ComOps calls this with just the productId.
     * We fetch the full product from ComOps ourselves and upsert it locally.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Product created / updated",
        description = "ComOps sends the `productId` of the changed product. This service fetches the full product from ComOps and upserts it locally.",
        requestBody = @RequestBody(
            content = @Content(
                examples = @ExampleObject(value = """
                    { "productId": "11111111-0000-0000-0000-000000000001" }
                    """)
            )
        )
    )
    public Mono<ProductResponse> onProductChanged(
            @RequestHeader("X-Webhook-Secret") String secret,
            @org.springframework.web.bind.annotation.RequestBody Map<String, UUID> body) {

        validateSecret(secret);
        UUID productId = body.get("productId");
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId is required");
        }
        log.info("Webhook received for productId: {}", productId);
        return productExternalService.fetchAndSaveProductById(productId);
    }

    /**
     * ComOps calls this when a product is deleted / deactivated.
     */
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Product deleted / deactivated",
        description = "ComOps notifies us that a product was removed. We mark it inactive locally."
    )
    public Mono<Void> onProductDeleted(
            @RequestHeader("X-Webhook-Secret") String secret,
            @PathVariable java.util.UUID productId) {

        validateSecret(secret);
        log.info("Webhook: product deleted {}", productId);
        return productExternalService.getProductById(productId)
                .flatMap(p -> {
                    p.setActive(false);
                    return productExternalService.saveProduct(p);
                })
                .then();
    }

    private void validateSecret(String provided) {
        if (!webhookSecret.equals(provided)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid webhook secret");
        }
    }
}
