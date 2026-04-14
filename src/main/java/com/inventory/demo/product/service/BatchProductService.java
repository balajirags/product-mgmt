package com.inventory.demo.product.service;

import com.inventory.demo.exception.BusinessRuleException;
import com.inventory.demo.exception.DomainException;
import com.inventory.demo.product.api.BatchItemResult;
import com.inventory.demo.product.api.BatchProductRequest;
import com.inventory.demo.product.api.BatchProductResponse;
import com.inventory.demo.product.api.CreateProductRequest;
import com.inventory.demo.product.api.ProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for batch product operations: create, update, and delete in bulk.
 * Each operation type runs in its own transaction.
 * Partial failures do not roll back successful operations.
 */
@Service
public class BatchProductService {

    private static final Logger log = LoggerFactory.getLogger(BatchProductService.class);

    private static final int MAX_BATCH_SIZE = 100;
    private static final String BATCH_SIZE_EXCEEDED_ERROR = "BATCH_SIZE_EXCEEDED";

    private final ProductService productService;

    public BatchProductService(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Executes batch product operations: creates first, then updates, then deletes.
     *
     * @param request the batch request with create, update, and delete arrays
     * @return composite response with per-item results for each operation type
     * @throws BusinessRuleException if the total batch size exceeds the limit
     */
    public BatchProductResponse executeBatch(BatchProductRequest request) {
        validateBatchSize(request);

        List<BatchItemResult> created = processBatchCreates(request.create());
        List<BatchItemResult> updated = processBatchUpdates(request.update());
        List<BatchItemResult> deleted = processBatchDeletes(request.delete());

        log.info("Batch operation completed: created={}/{}, updated={}/{}, deleted={}/{}",
                countSuccesses(created), request.create().size(),
                countSuccesses(updated), request.update().size(),
                countSuccesses(deleted), request.delete().size());

        return new BatchProductResponse(created, updated, deleted);
    }

    /**
     * Processes batch create operations, one product at a time.
     * Each item is processed independently; failures do not affect other items.
     *
     * @param createRequests the list of product creation requests
     * @return results for each create operation
     */
    @Transactional
    public List<BatchItemResult> processBatchCreates(List<CreateProductRequest> createRequests) {
        List<BatchItemResult> results = new ArrayList<>();
        for (CreateProductRequest createReq : createRequests) {
            results.add(processCreateItem(createReq));
        }
        return results;
    }

    /**
     * Processes batch update operations, one product at a time.
     * Each item is processed independently; failures do not affect other items.
     *
     * @param updateItems the list of update items with product IDs and data
     * @return results for each update operation
     */
    @Transactional
    public List<BatchItemResult> processBatchUpdates(List<BatchProductRequest.BatchUpdateItem> updateItems) {
        List<BatchItemResult> results = new ArrayList<>();
        for (BatchProductRequest.BatchUpdateItem updateItem : updateItems) {
            results.add(processUpdateItem(updateItem));
        }
        return results;
    }

    /**
     * Processes batch delete (soft-delete) operations, one product at a time.
     * Each item is processed independently; failures do not affect other items.
     *
     * @param deleteIds the list of product IDs to delete
     * @return results for each delete operation
     */
    @Transactional
    public List<BatchItemResult> processBatchDeletes(List<UUID> deleteIds) {
        List<BatchItemResult> results = new ArrayList<>();
        for (UUID id : deleteIds) {
            results.add(processDeleteItem(id));
        }
        return results;
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException") // Intentional: catch-all for per-item isolation
    private BatchItemResult processCreateItem(CreateProductRequest createReq) {
        try {
            ProductResponse product = productService.createProduct(createReq);
            return BatchItemResult.success(product);
        } catch (Exception ex) {
            log.warn("Batch create failed for title={}: {}", createReq.title(), ex.getMessage());
            return BatchItemResult.failure(null, resolveErrorCode(ex), ex.getMessage());
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException") // Intentional: catch-all for per-item isolation
    private BatchItemResult processUpdateItem(BatchProductRequest.BatchUpdateItem updateItem) {
        try {
            ProductResponse product = productService.updateProduct(updateItem.id(), updateItem.data());
            return BatchItemResult.success(product);
        } catch (Exception ex) {
            log.warn("Batch update failed for id={}: {}", updateItem.id(), ex.getMessage());
            return BatchItemResult.failure(updateItem.id(), resolveErrorCode(ex), ex.getMessage());
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException") // Intentional: catch-all for per-item isolation
    private BatchItemResult processDeleteItem(UUID id) {
        try {
            productService.softDeleteProduct(id);
            return BatchItemResult.deleted(id);
        } catch (Exception ex) {
            log.warn("Batch delete failed for id={}: {}", id, ex.getMessage());
            return BatchItemResult.failure(id, resolveErrorCode(ex), ex.getMessage());
        }
    }

    private void validateBatchSize(BatchProductRequest request) {
        int totalItems = request.create().size() + request.update().size() + request.delete().size();
        if (totalItems > MAX_BATCH_SIZE) {
            throw new BusinessRuleException(BATCH_SIZE_EXCEEDED_ERROR,
                    "Total batch size " + totalItems + " exceeds maximum of " + MAX_BATCH_SIZE);
        }
    }

    private String resolveErrorCode(Exception ex) {
        if (ex instanceof DomainException domainEx) {
            return domainEx.getErrorCode();
        }
        return "INTERNAL_ERROR";
    }

    private long countSuccesses(List<BatchItemResult> results) {
        return results.stream().filter(BatchItemResult::success).count();
    }
}
