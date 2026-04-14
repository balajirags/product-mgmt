package com.inventory.demo.product.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.demo.exception.BusinessRuleException;
import com.inventory.demo.exception.GlobalExceptionHandler;
import com.inventory.demo.product.service.BatchProductService;
import com.inventory.demo.product.service.ProductService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProductController.class, GlobalExceptionHandler.class})
@ContextConfiguration(classes = {ProductController.class, GlobalExceptionHandler.class})
class BatchProductControllerTest {

    private static final String BATCH_URL = "/api/v1/products/batch";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private BatchProductService batchProductService;

    private static ProductResponse sampleResponse(String title) {
        return new ProductResponse(
                UUID.randomUUID(), title, title.toLowerCase().replace(' ', '-'),
                "DRAFT", null, null, false, true,
                null, null, null, null, null, null,
                Instant.now(), Instant.now(), List.of(), List.of());
    }

    @Nested
    class SuccessCases {

        @Test
        void shouldReturnOk_whenBatchCreateProducts() throws Exception {
            // given
            ProductResponse created = sampleResponse("Product A");
            BatchProductResponse batchResponse = new BatchProductResponse(
                    List.of(BatchItemResult.success(created)), List.of(), List.of());
            when(batchProductService.executeBatch(any(BatchProductRequest.class)))
                    .thenReturn(batchResponse);

            String requestJson = """
                    {
                        "create": [{"title": "Product A"}],
                        "update": [],
                        "delete": []
                    }
                    """;

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.created").isArray())
                    .andExpect(jsonPath("$.created[0].success").value(true))
                    .andExpect(jsonPath("$.created[0].product.title").value("Product A"))
                    .andExpect(jsonPath("$.updated").isEmpty())
                    .andExpect(jsonPath("$.deleted").isEmpty());
        }

        @Test
        void shouldReturnOk_whenMixedBatchOperations() throws Exception {
            // given
            UUID deleteId = UUID.randomUUID();
            ProductResponse created = sampleResponse("New Product");
            ProductResponse updated = sampleResponse("Updated Product");
            BatchProductResponse batchResponse = new BatchProductResponse(
                    List.of(BatchItemResult.success(created)),
                    List.of(BatchItemResult.success(updated)),
                    List.of(BatchItemResult.deleted(deleteId)));
            when(batchProductService.executeBatch(any(BatchProductRequest.class)))
                    .thenReturn(batchResponse);

            String requestJson = String.format("""
                    {
                        "create": [{"title": "New Product"}],
                        "update": [{"id": "%s", "data": {"title": "Updated Product"}}],
                        "delete": ["%s"]
                    }
                    """, UUID.randomUUID(), deleteId);

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.created").hasJsonPath())
                    .andExpect(jsonPath("$.updated").hasJsonPath())
                    .andExpect(jsonPath("$.deleted").hasJsonPath())
                    .andExpect(jsonPath("$.created[0].success").value(true))
                    .andExpect(jsonPath("$.updated[0].success").value(true))
                    .andExpect(jsonPath("$.deleted[0].success").value(true));
        }

        @Test
        void shouldReturnOk_whenEmptyArrays() throws Exception {
            // given
            BatchProductResponse batchResponse = new BatchProductResponse(
                    List.of(), List.of(), List.of());
            when(batchProductService.executeBatch(any(BatchProductRequest.class)))
                    .thenReturn(batchResponse);

            String requestJson = """
                    {
                        "create": [],
                        "update": [],
                        "delete": []
                    }
                    """;

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.created").isEmpty())
                    .andExpect(jsonPath("$.updated").isEmpty())
                    .andExpect(jsonPath("$.deleted").isEmpty());
        }

        @Test
        void shouldReturnOk_whenPartialArraysProvided() throws Exception {
            // given
            ProductResponse created = sampleResponse("Only Creates");
            BatchProductResponse batchResponse = new BatchProductResponse(
                    List.of(BatchItemResult.success(created)), List.of(), List.of());
            when(batchProductService.executeBatch(any(BatchProductRequest.class)))
                    .thenReturn(batchResponse);

            String requestJson = """
                    {
                        "create": [{"title": "Only Creates"}]
                    }
                    """;

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.created[0].success").value(true));
        }
    }

    @Nested
    class FailureCases {

        @Test
        void shouldReturnConflict_whenBatchSizeExceeded() throws Exception {
            // given
            when(batchProductService.executeBatch(any(BatchProductRequest.class)))
                    .thenThrow(new BusinessRuleException("BATCH_SIZE_EXCEEDED",
                            "Total batch size 101 exceeds maximum of 100"));

            // build JSON with 101 delete IDs
            StringBuilder idsBuilder = new StringBuilder("[");
            for (int i = 0; i < 101; i++) {
                if (i > 0) {
                    idsBuilder.append(",");
                }
                idsBuilder.append("\"").append(UUID.randomUUID()).append("\"");
            }
            idsBuilder.append("]");

            String requestJson = """
                    {
                        "create": [],
                        "update": [],
                        "delete": %s
                    }
                    """.formatted(idsBuilder.toString());

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("Total batch size 101 exceeds maximum of 100"));
        }

        @Test
        void shouldReturnOk_withPartialFailuresInResponse() throws Exception {
            // given
            ProductResponse created = sampleResponse("Good Product");
            BatchProductResponse batchResponse = new BatchProductResponse(
                    List.of(
                            BatchItemResult.success(created),
                            BatchItemResult.failure(null, "DUPLICATE_HANDLE", "Handle already exists")),
                    List.of(), List.of());
            when(batchProductService.executeBatch(any(BatchProductRequest.class)))
                    .thenReturn(batchResponse);

            String requestJson = """
                    {
                        "create": [{"title": "Good Product"}, {"title": "Duplicate"}]
                    }
                    """;

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.created[0].success").value(true))
                    .andExpect(jsonPath("$.created[1].success").value(false))
                    .andExpect(jsonPath("$.created[1].error_code").value("DUPLICATE_HANDLE"))
                    .andExpect(jsonPath("$.created[1].error_message").value("Handle already exists"));
        }
    }

    @Nested
    class ValidationCases {

        @Test
        void shouldReturnBadRequest_whenCreateItemMissingTitle() throws Exception {
            // given — nested @Valid should catch missing title in create array items
            String requestJson = """
                    {
                        "create": [{"handle": "no-title-product"}]
                    }
                    """;

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }
    }
}
