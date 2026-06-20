package com.example.account.modules.facturation.service.ExternalServices;

import com.example.account.modules.facturation.dto.request.ExternalRequest.ReserveRequest;
import com.example.account.modules.facturation.dto.response.ExternalResponses.ProductResponse;
import com.example.account.modules.facturation.mapper.ProductMapper;
import com.example.account.modules.facturation.repository.ProductRepository;
import com.example.account.modules.facturation.service.ExternalServices.entity.Product;
import com.example.account.modules.facturation.service.ExternalServices.entity.WebsocketUtils.StockBroadcaster;

import ch.qos.logback.core.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductExternalService {

    private final WebClient.Builder webClientBuilder;
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final RedisService redisService;
    private final StockBroadcaster broadcaster;
    @Value("${comops.core.products}")
    private String comOpsUrl;

    /**
     * Fetches products from external API and saves them to the DB
     */
    public Flux<ProductResponse> fetchAndSaveProducts(UUID organizationId) {
        return getProductsByOrganization(organizationId)
                .flatMap(this::saveProduct) // Save each product as it is received

                .doOnComplete(() -> log.info("Sync complete for org: {}", organizationId));
    }

    public Mono<ProductResponse> fetchAndSaveProductById(UUID productId) {
        String url = String.format("http://%s/api/products/%s", comOpsUrl, productId);
        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("API Error: " + error)))
                )
                .bodyToMono(ProductResponse.class)
                .doOnNext(dto -> log.info("Fetched from ComOps: {}", dto.getNomProduit()))
                .flatMap(this::saveProduct);
    }

    public Flux<ProductResponse> getProductsByOrganization(UUID organizationId) {
        String url = String.format("http://%s/api/products/organizations/%s", comOpsUrl, organizationId);

        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                            .flatMap(error -> Mono.error(new RuntimeException("API Error: " + error)))
                )
                .bodyToFlux(ProductResponse.class)
                .doOnNext(dto -> log.info("Received from API: {}", dto.getNomProduit()));
    }
   public Mono<ProductResponse> saveProduct(ProductResponse dto) {
    // 1. Try to find the existing product by its unique business ID (idProduit)
    return productRepository.findByUuid(dto.getIdProduit())
            .flatMap(existingProduct -> {
                log.info("🔄 Updating existing product: {}", dto.getNomProduit());
                Product product=productMapper.toEntity(dto);
                // 2. Map incoming DTO values to the existing entity
                existingProduct.setName(product.getName());
                existingProduct.setType(product.getType());
                existingProduct.setSalePrice(product.getSalePrice());
                existingProduct.setCost(product.getCost());
                existingProduct.setCategory(product.getCategory());
                existingProduct.setReference(product.getReference());
                existingProduct.setBarcode(product.getBarcode());
                existingProduct.setPhoto(product.getPhoto());
                existingProduct.setActive(product.getActive());
                existingProduct.setUpdatedAt(LocalDate.now());
                
                // Handling complex lists (JSON columns)
                existingProduct.setAllowedSaleSizes(product.getAllowedSaleSizes());
                existingProduct.setActivePromotions(product.getActivePromotions());

                // Logic for Stock Sync: 
                // We update the absolute stock quantity from the source
                existingProduct.setStockQuantity(product.getStockQuantity());
                // Note: You might want to recalculate available_quantity here 
                // based on your local reserved_quantity.
                
                return productRepository.save(existingProduct);
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.info("✨ Creating new product: {}", dto.getNomProduit());
                
                // 3. Map DTO to a brand new Entity
                Product newProduct = productMapper.toEntity(dto);
                
                // Ensure the IDs and initial values are set correctly
                newProduct.setId(dto.getIdProduit()); // Setting the UUID from the source
                newProduct.setOrganizationId(dto.getOrganizationId());
                newProduct.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0.0);
                newProduct.setAvailableQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0.0);
                newProduct.setReservedQuantity(0.0);
                newProduct.setCreatedAt(LocalDate.now());
                
                return productRepository.save(newProduct);
            }))
            .map(productMapper::toResponse)
            .doOnError(e -> log.error("❌ Failed to upsert product {}: {}", dto.getNomProduit(), e.getMessage()));
}




    public Flux<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .map(productMapper::toResponse);
    }

    public Flux<ProductResponse> getAllProductsForOrganization(UUID orgId) {
        return productRepository.findByOrganizationId(orgId)
                .map(productMapper::toResponse);
    }
   public Mono<ProductResponse> getProductById(UUID id) {
    return productRepository.findByUuid(id)
            // 1. Use doOnNext for logging side-effects
            .doOnNext(product -> System.out.println("Fetched Product: " + product))
            
            // 2. Transform the entity to Response DTO
            .map(productMapper::toResponse)
            
            // 3. Handle "Not Found" cases explicitly
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.SC_NOT_FOUND, "Product not found",null)));
}
    @Transactional
    public Mono<Void> reserveProduct(ReserveRequest request){

            //first find the product
            return productRepository.findByUuid(request.getProductId())
                    .switchIfEmpty(Mono.error(new RuntimeException("Product not found with id: " + request.getProductId())))
                    .flatMap(product -> {

                        //construct redis key ie sellerid:productid:qty->qty
                        String key=String.format("res:%s:%s:%s",request.getSellerId(),request.getProductId(),request.getQuantity());


                        // 1. Validation Guard: Check if enough stock is available
               // 1. Validation Guard: Check if enough stock is available
                double requestedAmount = Double.parseDouble(request.getQuantity().toString());

                if (product.getAvailableQuantity() < requestedAmount) {
                    // Explicitly casting to Throwable or using a more direct approach
                    return Mono.error(new RuntimeException("OUT_OF_STOCK"));
                }
                                        //store the reservation in redis with a TTL (e.g., 15 minutes)
                        redisService.save(key,request.getQuantity().toString(),Integer.valueOf(2));

                        //update db availaible and reserved quatnites

                        product.setAvailableQuantity(product.getAvailableQuantity()-Double.valueOf(request.getQuantity()));
                        product.setReservedQuantity(product.getReservedQuantity()+Double.valueOf(request.getQuantity()));

                         return redisService.save(key, request.getQuantity().toString(), 2)
                .then(productRepository.save(product))
                .doOnNext(savedProduct -> {
                    // 4. Trigger the Real-time update
                    broadcaster.broadcast(productMapper.toResponse(savedProduct));
                })
                .doOnSuccess(v -> log.info("Successfully reserved {} for seller {}", request.getProductId(), request.getSellerId()))
                .then();
                  
                    });

    }

   @Transactional
public Mono<Void> releaseProduct(UUID productId, Integer quantity) {
    return productRepository.findByUuid(productId)
            .switchIfEmpty(Mono.error(new RuntimeException("Product not found with id: " + productId)))
            .flatMap(product -> {
                log.info("Restoring {} units to product: {}", quantity, product.getName());

                // 1. Logic: Add back to Available, Subtract from Reserved
                // Use Math.max to prevent negative reserved quantities
                double currentReserved = product.getReservedQuantity() != null ? product.getReservedQuantity() : 0.0;
                double currentAvailable = product.getAvailableQuantity() != null ? product.getAvailableQuantity() : 0.0;

                product.setReservedQuantity(Math.max(0, currentReserved - quantity));
                product.setAvailableQuantity(currentAvailable + quantity);

                // 2. Save and Broadcast the update
                return productRepository.save(product)
                        .doOnNext(savedProduct -> {
                            // Notify all connected clients that stock is back
                            broadcaster.broadcast(productMapper.toResponse(savedProduct));
                        })
                        .doOnSuccess(v -> log.info("Successfully released stock for product {}", productId))
                        .then(); // Return Mono<Void>
            });
}


 @Transactional
public Mono<Void> releaseProductsForSeller(UUID sellerId) {
    String pattern = String.format("res:%s:*", sellerId);

    // ✅ Added 'return' at the start of the chain
    return redisService.getKeys(pattern)
                .flatMap(key -> {
                    // ✅ Added 'return' here to ensure the delete operation is executed
                    return redisService.deleteKey(key);
                })
                .then() // Wait for all deletions to finish
                .doOnSuccess(v -> log.info("Successfully cleared all reservations for seller: {}", sellerId));
}

}