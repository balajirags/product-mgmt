package com.inventory.demo.product.api;

import com.inventory.demo.product.service.BatchProductService;
import com.inventory.demo.product.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for product management endpoints.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final BatchProductService batchProductService;

    public ProductController(ProductService productService, BatchProductService batchProductService) {
        this.productService = productService;
        this.batchProductService = batchProductService;
    }

    /**
     * Creates a new product.
     *
     * @param request the product creation request with mandatory title
     * @return the created product with HTTP 201
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("POST /api/v1/products - Creating product: title={}", request.title());
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists products with optional filtering by status and pagination.
     *
     * @param status   optional product status filter
     * @param pageable pagination and sort parameters
     * @return paginated list of products with HTTP 200
     */
    @GetMapping
    public ResponseEntity<PagedProductResponse> listProducts(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        log.info("GET /api/v1/products - Listing products: status={}, page={}, size={}",
                status, pageable.getPageNumber(), pageable.getPageSize());
        PagedProductResponse response = productService.listProducts(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id the product UUID
     * @return the product with HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        log.info("GET /api/v1/products/{} - Retrieving product", id);
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing product with partial update semantics.
     * Only non-null fields in the request body are applied.
     *
     * @param id      the product UUID
     * @param request the update request with optional fields
     * @return the updated product with HTTP 200
     */
    @PostMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @RequestBody UpdateProductRequest request) {
        log.info("POST /api/v1/products/{} - Updating product", id);
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Executes batch product operations: create, update, and delete in a single call.
     * Operations execute in order: creates first, then updates, then deletes.
     * Partial failures do not roll back successful operations.
     *
     * @param request the batch request with create, update, and delete arrays
     * @return composite response with per-item results for each operation type
     */
    @PostMapping("/batch")
    public ResponseEntity<BatchProductResponse> batchProducts(
            @Valid @RequestBody BatchProductRequest request) {
        log.info("POST /api/v1/products/batch - Batch operation: creates={}, updates={}, deletes={}",
                request.create().size(), request.update().size(), request.delete().size());
        BatchProductResponse response = batchProductService.executeBatch(request);
        return ResponseEntity.ok(response);
    }
}
