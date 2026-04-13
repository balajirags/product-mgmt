package com.inventory.demo.product.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.demo.exception.BusinessRuleException;
import com.inventory.demo.exception.GlobalExceptionHandler;
import com.inventory.demo.product.service.ProductService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProductController.class, GlobalExceptionHandler.class})
@ContextConfiguration(classes = {ProductController.class, GlobalExceptionHandler.class})
class ProductControllerTest {

    private static final String PRODUCTS_URL = "/api/v1/products";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private static ProductResponse sampleResponse() {
        return new ProductResponse(
                UUID.randomUUID(),
                "Test Product",
                "test-product",
                "DRAFT",
                "A description",
                "A subtitle",
                false,
                true,
                new BigDecimal("1.5"),
                new BigDecimal("10.0"),
                new BigDecimal("5.0"),
                new BigDecimal("3.0"),
                "{\"key\":\"value\"}",
                "EXT-001",
                Instant.now(),
                Instant.now()
        );
    }

    @Nested
    class SuccessCases {

        @Test
        void shouldReturnCreated_whenValidRequestWithTitleOnly() throws Exception {
            // given
            when(productService.createProduct(any(CreateProductRequest.class)))
                    .thenReturn(sampleResponse());

            String requestJson = """
                    {
                        "title": "Test Product"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Test Product"))
                    .andExpect(jsonPath("$.handle").value("test-product"))
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.is_giftcard").value(false))
                    .andExpect(jsonPath("$.discountable").value(true));
        }

        @Test
        void shouldReturnCreated_whenFullRequestProvided() throws Exception {
            // given
            when(productService.createProduct(any(CreateProductRequest.class)))
                    .thenReturn(sampleResponse());

            String requestJson = """
                    {
                        "title": "Test Product",
                        "handle": "custom-handle",
                        "status": "PUBLISHED",
                        "description": "A description",
                        "subtitle": "A subtitle",
                        "weight": 1.5,
                        "height": 10.0,
                        "width": 5.0,
                        "length": 3.0,
                        "metadata": "{\\"key\\":\\"value\\"}",
                        "external_id": "EXT-001"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.description").value("A description"))
                    .andExpect(jsonPath("$.subtitle").value("A subtitle"))
                    .andExpect(jsonPath("$.weight").value(1.5))
                    .andExpect(jsonPath("$.external_id").value("EXT-001"));
        }

        @Test
        void shouldReturnSnakeCaseResponseFields() throws Exception {
            // given
            when(productService.createProduct(any(CreateProductRequest.class)))
                    .thenReturn(sampleResponse());

            String requestJson = """
                    {
                        "title": "Test Product"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.is_giftcard").exists())
                    .andExpect(jsonPath("$.external_id").exists())
                    .andExpect(jsonPath("$.created_at").exists())
                    .andExpect(jsonPath("$.updated_at").exists());
        }
    }

    @Nested
    class ValidationCases {

        @Test
        void shouldReturnBadRequest_whenTitleIsMissing() throws Exception {
            // given
            String requestJson = """
                    {
                        "handle": "some-handle"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"));

            verify(productService, never()).createProduct(any());
        }

        @Test
        void shouldReturnBadRequest_whenTitleIsBlank() throws Exception {
            // given
            String requestJson = """
                    {
                        "title": "   "
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"));

            verify(productService, never()).createProduct(any());
        }

        @Test
        void shouldReturnBadRequest_whenEmptyBody() throws Exception {
            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"));

            verify(productService, never()).createProduct(any());
        }
    }

    @Nested
    class BusinessRuleCases {

        @Test
        void shouldReturnConflict_whenDuplicateHandle() throws Exception {
            // given
            when(productService.createProduct(any(CreateProductRequest.class)))
                    .thenThrow(new BusinessRuleException("DUPLICATE_HANDLE",
                            "A product with handle 'existing' already exists"));

            String requestJson = """
                    {
                        "title": "Test Product",
                        "handle": "existing"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("DUPLICATE_HANDLE"))
                    .andExpect(jsonPath("$.detail").value("A product with handle 'existing' already exists"));
        }

        @Test
        void shouldReturnConflict_whenInvalidStatus() throws Exception {
            // given
            when(productService.createProduct(any(CreateProductRequest.class)))
                    .thenThrow(new BusinessRuleException("INVALID_STATUS",
                            "Invalid product status: BOGUS"));

            String requestJson = """
                    {
                        "title": "Test Product",
                        "status": "BOGUS"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("INVALID_STATUS"));
        }
    }
}
