package com.inventory.demo.product.service;

import com.inventory.demo.exception.BusinessRuleException;
import com.inventory.demo.exception.ResourceNotFoundException;
import com.inventory.demo.product.api.PagedProductResponse;
import com.inventory.demo.product.api.CreateProductRequest;
import com.inventory.demo.product.api.ProductResponse;
import com.inventory.demo.product.api.UpdateProductRequest;
import com.inventory.demo.product.domain.Product;
import com.inventory.demo.product.domain.ProductStatus;
import com.inventory.demo.product.repository.ProductRepository;
import com.inventory.demo.product.repository.ProductSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

/**
 * Service for managing product creation and related business operations.
 */
@Service
@SuppressWarnings("PMD.GodClass") // Service handles full product CRUD lifecycle
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private static final String DUPLICATE_HANDLE_ERROR = "DUPLICATE_HANDLE";
    private static final String INVALID_STATUS_ERROR = "INVALID_STATUS";
    private static final String BLANK_TITLE_ERROR = "BLANK_TITLE";
    private static final String SLUG_SEPARATOR = "-";
    private static final String NON_ALPHANUMERIC_PATTERN = "[^a-z0-9\\-]";
    private static final String CONSECUTIVE_HYPHENS_PATTERN = "-{2,}";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Creates a new product in DRAFT status (or the explicitly requested status).
     *
     * @param request the product creation request
     * @return the created product as a response DTO
     * @throws BusinessRuleException if the handle is a duplicate or the status is invalid
     */
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product: title={}", request.title());

        String handle = resolveHandle(request.title(), request.handle());
        checkForDuplicateHandle(handle);

        Product product = Product.create(request.title(), handle);

        applyOptionalStatus(product, request.status());
        applyOptionalFields(product, request);

        Product saved = productRepository.save(product);
        log.info("Product created successfully: id={}, handle={}, status={}",
                saved.getId(), saved.getHandle(), saved.getStatus());

        return ProductResponse.fromEntity(saved);
    }

    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id the product UUID
     * @return the product as a response DTO
     * @throws ResourceNotFoundException if no product exists with the given ID
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        log.info("Retrieving product: id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        return ProductResponse.fromEntity(product);
    }

    /**
     * Lists products with optional status filtering and pagination.
     *
     * @param status   optional status filter (e.g., DRAFT, PUBLISHED)
     * @param pageable pagination and sort parameters
     * @return a paginated response of products
     * @throws BusinessRuleException if the status filter value is invalid
     */
    @Transactional(readOnly = true)
    public PagedProductResponse listProducts(String status, Pageable pageable) {
        log.info("Listing products: status={}, page={}, size={}", status, pageable.getPageNumber(), pageable.getPageSize());

        Pageable cappedPageable = capPageSize(pageable);
        Specification<Product> spec = buildSpecification(status);

        Page<ProductResponse> productPage = productRepository.findAll(spec, cappedPageable)
                .map(ProductResponse::fromEntity);

        log.info("Products listed: totalElements={}, totalPages={}", productPage.getTotalElements(), productPage.getTotalPages());
        return PagedProductResponse.fromPage(productPage);
    }

    /**
     * Updates an existing product with partial update semantics.
     * Only non-null fields in the request are applied; null fields are left unchanged.
     *
     * @param id      the product UUID
     * @param request the update request with optional fields
     * @return the updated product as a response DTO
     * @throws ResourceNotFoundException if no product exists with the given ID
     * @throws BusinessRuleException     if the handle is a duplicate or the status is invalid
     */
    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        log.info("Updating product: id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        boolean modified = applyUpdates(product, id, request);

        if (modified) {
            product.markUpdated();
            Product saved = productRepository.save(product);
            log.info("Product updated successfully: id={}, handle={}", saved.getId(), saved.getHandle());
            return ProductResponse.fromEntity(saved);
        }

        log.info("No changes applied to product: id={}", id);
        return ProductResponse.fromEntity(product);
    }

    /**
     * Generates a URL-friendly slug from a title string.
     * Converts to lowercase, replaces spaces and non-alphanumeric characters with hyphens,
     * and trims leading/trailing hyphens.
     *
     * @param title the product title to slugify
     * @return a URL-friendly slug
     */
    String generateSlug(String title) {
        return title.toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", SLUG_SEPARATOR)
                .replaceAll(NON_ALPHANUMERIC_PATTERN, "")
                .replaceAll(CONSECUTIVE_HYPHENS_PATTERN, SLUG_SEPARATOR)
                .replaceAll("^-|-$", "");
    }

    private String resolveHandle(String title, String explicitHandle) {
        if (explicitHandle != null && !explicitHandle.isBlank()) {
            return explicitHandle.trim();
        }
        return generateSlug(title);
    }

    private void checkForDuplicateHandle(String handle) {
        if (productRepository.existsByHandle(handle)) {
            log.warn("Duplicate product handle attempted: {}", handle);
            throw new BusinessRuleException(DUPLICATE_HANDLE_ERROR,
                    "A product with handle '" + handle + "' already exists");
        }
    }

    private void checkForDuplicateHandleExcluding(String handle, UUID excludeId) {
        if (productRepository.existsByHandleAndIdNot(handle, excludeId)) {
            log.warn("Duplicate product handle attempted on update: {}", handle);
            throw new BusinessRuleException(DUPLICATE_HANDLE_ERROR,
                    "A product with handle '" + handle + "' already exists");
        }
    }

    private void applyOptionalStatus(Product product, String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return;
        }
        try {
            ProductStatus requestedStatus = ProductStatus.valueOf(
                    statusValue.toUpperCase(Locale.ROOT));
            product.applyStatus(requestedStatus);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid product status attempted: {}", statusValue);
            throw new BusinessRuleException(INVALID_STATUS_ERROR,
                    "Invalid product status: " + statusValue
                            + ". Valid statuses are: DRAFT, PUBLISHED, PROPOSED, REJECTED", ex);
        }
    }

    private void applyOptionalFields(Product product, CreateProductRequest request) {
        if (request.description() != null) {
            product.describeAs(request.description());
        }
        if (request.subtitle() != null) {
            product.assignSubtitle(request.subtitle());
        }
        if (hasDimensions(request)) {
            product.applyDimensions(request.weight(), request.height(),
                    request.width(), request.length());
        }
        if (request.metadata() != null) {
            product.attachMetadata(request.metadata());
        }
        if (request.externalId() != null) {
            product.assignExternalId(request.externalId());
        }
    }

    @SuppressWarnings("PMD.CognitiveComplexity") // Simple null-check sequence for partial update
    private boolean applyUpdates(Product product, UUID productId, UpdateProductRequest request) {
        boolean titleChanged = applyTitleUpdate(product, request);
        boolean handleChanged = applyHandleUpdate(product, productId, request);
        boolean statusChanged = applyStatusUpdate(product, request);
        boolean fieldsChanged = applyFieldUpdates(product, request);

        return titleChanged || handleChanged || statusChanged || fieldsChanged;
    }

    private boolean applyTitleUpdate(Product product, UpdateProductRequest request) {
        if (request.title() == null) {
            return false;
        }
        validateTitle(request.title());
        product.updateTitle(request.title());
        return true;
    }

    private boolean applyHandleUpdate(Product product, UUID productId, UpdateProductRequest request) {
        if (request.handle() == null) {
            return false;
        }
        checkForDuplicateHandleExcluding(request.handle().trim(), productId);
        product.updateHandle(request.handle().trim());
        return true;
    }

    private boolean applyStatusUpdate(Product product, UpdateProductRequest request) {
        if (request.status() == null) {
            return false;
        }
        applyOptionalStatus(product, request.status());
        return true;
    }

    private boolean applyFieldUpdates(Product product, UpdateProductRequest request) {
        boolean textUpdated = applyTextFieldUpdates(product, request);
        boolean dimsUpdated = applyDimensionUpdates(product, request);
        boolean metaUpdated = applyMetadataUpdates(product, request);

        return textUpdated || dimsUpdated || metaUpdated;
    }

    private boolean applyTextFieldUpdates(Product product, UpdateProductRequest request) {
        boolean modified = false;

        if (request.description() != null) {
            product.describeAs(request.description());
            modified = true;
        }

        if (request.subtitle() != null) {
            product.assignSubtitle(request.subtitle());
            modified = true;
        }

        return modified;
    }

    private boolean applyDimensionUpdates(Product product, UpdateProductRequest request) {
        if (!hasUpdateDimensions(request)) {
            return false;
        }
        product.applyDimensions(
                request.weight() != null ? request.weight() : product.getWeight(),
                request.height() != null ? request.height() : product.getHeight(),
                request.width() != null ? request.width() : product.getWidth(),
                request.length() != null ? request.length() : product.getLength()
        );
        return true;
    }

    private boolean applyMetadataUpdates(Product product, UpdateProductRequest request) {
        boolean modified = false;

        if (request.metadata() != null) {
            product.attachMetadata(request.metadata());
            modified = true;
        }

        if (request.externalId() != null) {
            product.assignExternalId(request.externalId());
            modified = true;
        }

        return modified;
    }

    private void validateTitle(String title) {
        if (title.isBlank()) {
            throw new BusinessRuleException(BLANK_TITLE_ERROR, "Product title must not be blank");
        }
    }

    private boolean hasUpdateDimensions(UpdateProductRequest request) {
        return request.weight() != null || request.height() != null
                || request.width() != null || request.length() != null;
    }

    private boolean hasDimensions(CreateProductRequest request) {
        return request.weight() != null || request.height() != null
                || request.width() != null || request.length() != null;
    }

    private Pageable capPageSize(Pageable pageable) {
        int size = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, DEFAULT_SORT_FIELD);
        return PageRequest.of(pageable.getPageNumber(), size, sort);
    }

    private Specification<Product> buildSpecification(String status) {
        if (status == null || status.isBlank()) {
            return Specification.where(null);
        }
        try {
            ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase(Locale.ROOT));
            return Specification.where(ProductSpecifications.withStatus(productStatus));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid product status filter: {}", status);
            throw new BusinessRuleException(INVALID_STATUS_ERROR,
                    "Invalid product status: " + status
                            + ". Valid statuses are: DRAFT, PUBLISHED, PROPOSED, REJECTED", ex);
        }
    }
}
