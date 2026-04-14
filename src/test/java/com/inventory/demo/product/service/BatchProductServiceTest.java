package com.inventory.demo.product.service;

import com.inventory.demo.exception.BusinessRuleException;
import com.inventory.demo.exception.ResourceNotFoundException;
import com.inventory.demo.product.api.BatchItemResult;
import com.inventory.demo.product.api.BatchProductRequest;
import com.inventory.demo.product.api.BatchProductResponse;
import com.inventory.demo.product.api.CreateProductRequest;
import com.inventory.demo.product.api.ProductResponse;
import com.inventory.demo.product.api.UpdateProductRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchProductServiceTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private BatchProductService batchProductService;

    private static CreateProductRequest createRequest(String title) {
        return new CreateProductRequest(title, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }

    private static ProductResponse sampleResponse(String title) {
        return new ProductResponse(
                UUID.randomUUID(), title, title.toLowerCase().replace(' ', '-'),
                "DRAFT", null, null, false, true,
                null, null, null, null, null, null,
                Instant.now(), Instant.now(), List.of(), List.of());
    }

    private static UpdateProductRequest updateRequest(String title) {
        return new UpdateProductRequest(title, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }

    @Nested
    class SuccessCases {

        @Test
        void shouldCreateMultipleProducts() {
            // given
            CreateProductRequest req1 = createRequest("Product A");
            CreateProductRequest req2 = createRequest("Product B");
            when(productService.createProduct(req1)).thenReturn(sampleResponse("Product A"));
            when(productService.createProduct(req2)).thenReturn(sampleResponse("Product B"));

            BatchProductRequest request = new BatchProductRequest(
                    List.of(req1, req2), List.of(), List.of());

            // when
            BatchProductResponse response = batchProductService.executeBatch(request);

            // then
            assertThat(response.created()).hasSize(2);
            assertThat(response.created()).allMatch(BatchItemResult::success);
            assertThat(response.updated()).isEmpty();
            assertThat(response.deleted()).isEmpty();
        }

        @Test
        void shouldUpdateMultipleProducts() {
            // given
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            UpdateProductRequest upd1 = updateRequest("Updated A");
            UpdateProductRequest upd2 = updateRequest("Updated B");
            when(productService.updateProduct(eq(id1), eq(upd1))).thenReturn(sampleResponse("Updated A"));
            when(productService.updateProduct(eq(id2), eq(upd2))).thenReturn(sampleResponse("Updated B"));

            BatchProductRequest request = new BatchProductRequest(
                    List.of(),
                    List.of(new BatchProductRequest.BatchUpdateItem(id1, upd1),
                            new BatchProductRequest.BatchUpdateItem(id2, upd2)),
                    List.of());

            // when
            BatchProductResponse response = batchProductService.executeBatch(request);

            // then
            assertThat(response.updated()).hasSize(2);
            assertThat(response.updated()).allMatch(BatchItemResult::success);
        }

        @Test
        void shouldDeleteMultipleProducts() {
            // given
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            doNothing().when(productService).softDeleteProduct(id1);
            doNothing().when(productService).softDeleteProduct(id2);

            BatchProductRequest request = new BatchProductRequest(
                    List.of(), List.of(), List.of(id1, id2));

            // when
            BatchProductResponse response = batchProductService.executeBatch(request);

            // then
            assertThat(response.deleted()).hasSize(2);
            assertThat(response.deleted()).allMatch(BatchItemResult::success);
            assertThat(response.deleted().get(0).id()).isEqualTo(id1);
            assertThat(response.deleted().get(1).id()).isEqualTo(id2);
        }

        @Test
        void shouldExecuteMixedBatchOperations() {
            // given
            CreateProductRequest createReq = createRequest("New Product");
            UUID updateId = UUID.randomUUID();
            UpdateProductRequest updateReq = updateRequest("Updated");
            UUID deleteId = UUID.randomUUID();

            when(productService.createProduct(createReq)).thenReturn(sampleResponse("New Product"));
            when(productService.updateProduct(eq(updateId), eq(updateReq))).thenReturn(sampleResponse("Updated"));
            doNothing().when(productService).softDeleteProduct(deleteId);

            BatchProductRequest request = new BatchProductRequest(
                    List.of(createReq),
                    List.of(new BatchProductRequest.BatchUpdateItem(updateId, updateReq)),
                    List.of(deleteId));

            // when
            BatchProductResponse response = batchProductService.executeBatch(request);

            // then
            assertThat(response.created()).hasSize(1);
            assertThat(response.updated()).hasSize(1);
            assertThat(response.deleted()).hasSize(1);
            assertThat(response.created().get(0).success()).isTrue();
            assertThat(response.updated().get(0).success()).isTrue();
            assertThat(response.deleted().get(0).success()).isTrue();
        }

        @Test
        void shouldHandleEmptyArrays() {
            // given
            BatchProductRequest request = new BatchProductRequest(
                    List.of(), List.of(), List.of());

            // when
            BatchProductResponse response = batchProductService.executeBatch(request);

            // then
            assertThat(response.created()).isEmpty();
            assertThat(response.updated()).isEmpty();
            assertThat(response.deleted()).isEmpty();
        }

        @Test
        void shouldHandlePartialArrays() {
            // given
            CreateProductRequest createReq = createRequest("Only Create");
            when(productService.createProduct(createReq)).thenReturn(sampleResponse("Only Create"));

            BatchProductRequest request = new BatchProductRequest(
                    List.of(createReq), null, null);

            // when
            BatchProductResponse response = batchProductService.executeBatch(request);

            // then
            assertThat(response.created()).hasSize(1);
            assertThat(response.updated()).isEmpty();
            assertThat(response.deleted()).isEmpty();
        }
    }

    @Nested
    class FailureCases {

        @Test
        void shouldIsolateCreateFailures() {
            // given
            CreateProductRequest validReq = createRequest("Valid Product");
            CreateProductRequest invalidReq = createRequest("Duplicate");
            when(productService.createProduct(validReq)).thenReturn(sampleResponse("Valid Product"));
            when(productService.createProduct(invalidReq))
                    .thenThrow(new BusinessRuleException("DUPLICATE_HANDLE", "Handle already exists"));

            BatchProductRequest request = new BatchProductRequest(
                    List.of(validReq, invalidReq), List.of(), List.of());

            // when
            BatchProductResponse response = batchProductService.executeBatch(request);

            // then
            assertThat(response.created()).hasSize(2);
            assertThat(response.created().get(0).success()).isTrue();
            assertThat(response.created().get(1).success()).isFalse();
            assertThat(response.created().get(1).errorCode()).isEqualTo("DUPLICATE_HANDLE");
            assertThat(response.created().get(1).errorMessage()).isEqualTo("Handle already exists");
        }

        @Test
        void shouldIsolateUpdateNotFoundFailure() {
            // given
            UUID existingId = UUID.randomUUID();
            UUID missingId = UUID.randomUUID();
            UpdateProductRequest upd = updateRequest("Update");
            when(productService.updateProduct(eq(existingId), any())).thenReturn(sampleResponse("Update"));
            when(productService.updateProduct(eq(missingId), any()))
                    .thenThrow(new ResourceNotFoundException("Product", missingId));

            BatchProductRequest request = new BatchProductRequest(
                    List.of(),
                    List.of(new BatchProductRequest.BatchUpdateItem(existingId, upd),
                            new BatchProductRequest.BatchUpdateItem(missingId, upd)),
                    List.of());

            // when
            BatchProductResponse response = batchProductService.executeBatch(request);

            // then
            assertThat(response.updated()).hasSize(2);
            assertThat(response.updated().get(0).success()).isTrue();
            assertThat(response.updated().get(1).success()).isFalse();
            assertThat(response.updated().get(1).id()).isEqualTo(missingId);
            assertThat(response.updated().get(1).errorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        }

        @Test
        void shouldIsolateDeleteNotFoundFailure() {
            // given
            UUID existingId = UUID.randomUUID();
            UUID missingId = UUID.randomUUID();
            doNothing().when(productService).softDeleteProduct(existingId);
            doThrow(new ResourceNotFoundException("Product", missingId))
                    .when(productService).softDeleteProduct(missingId);

            BatchProductRequest request = new BatchProductRequest(
                    List.of(), List.of(), List.of(existingId, missingId));

            // when
            BatchProductResponse response = batchProductService.executeBatch(request);

            // then
            assertThat(response.deleted()).hasSize(2);
            assertThat(response.deleted().get(0).success()).isTrue();
            assertThat(response.deleted().get(1).success()).isFalse();
            assertThat(response.deleted().get(1).id()).isEqualTo(missingId);
        }
    }

    @Nested
    class ValidationCases {

        @Test
        void shouldRejectBatchExceedingMaxSize() {
            // given — create a request with 101 items total
            List<UUID> deleteIds = new java.util.ArrayList<>();
            for (int i = 0; i < 101; i++) {
                deleteIds.add(UUID.randomUUID());
            }
            BatchProductRequest request = new BatchProductRequest(
                    List.of(), List.of(), deleteIds);

            // when / then
            assertThatThrownBy(() -> batchProductService.executeBatch(request))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("exceeds maximum of 100");
        }

        @Test
        void shouldAcceptBatchAtMaxSize() {
            // given — exactly 100 items
            List<UUID> deleteIds = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                deleteIds.add(UUID.randomUUID());
            }
            // Stub all deletes
            doNothing().when(productService).softDeleteProduct(any(UUID.class));

            BatchProductRequest request = new BatchProductRequest(
                    List.of(), List.of(), deleteIds);

            // when
            BatchProductResponse response = batchProductService.executeBatch(request);

            // then
            assertThat(response.deleted()).hasSize(100);
            verify(productService, times(100)).softDeleteProduct(any(UUID.class));
        }
    }

    @Nested
    class ExecutionOrderCases {

        @Test
        void shouldExecuteCreatesBeforeUpdatesBeforeDeletes() {
            // given
            CreateProductRequest createReq = createRequest("Created");
            UUID updateId = UUID.randomUUID();
            UpdateProductRequest updateReq = updateRequest("Updated");
            UUID deleteId = UUID.randomUUID();

            when(productService.createProduct(createReq)).thenReturn(sampleResponse("Created"));
            when(productService.updateProduct(eq(updateId), eq(updateReq))).thenReturn(sampleResponse("Updated"));
            doNothing().when(productService).softDeleteProduct(deleteId);

            BatchProductRequest request = new BatchProductRequest(
                    List.of(createReq),
                    List.of(new BatchProductRequest.BatchUpdateItem(updateId, updateReq)),
                    List.of(deleteId));

            // when
            batchProductService.executeBatch(request);

            // then — verify execution order
            var inOrder = org.mockito.Mockito.inOrder(productService);
            inOrder.verify(productService).createProduct(createReq);
            inOrder.verify(productService).updateProduct(eq(updateId), eq(updateReq));
            inOrder.verify(productService).softDeleteProduct(deleteId);
        }
    }
}
