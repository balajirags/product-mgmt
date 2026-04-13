package com.inventory.demo.product.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.demo.exception.BusinessRuleException;
import com.inventory.demo.exception.GlobalExceptionHandler;
import com.inventory.demo.exception.ResourceNotFoundException;
import com.inventory.demo.product.service.ProductService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Nested
    class GetProductByIdSuccessCases {

        @Test
        void shouldReturnOk_whenProductExists() throws Exception {
            // given
            UUID productId = UUID.randomUUID();
            ProductResponse response = sampleResponse();
            when(productService.getProductById(productId)).thenReturn(response);

            // when / then
            mockMvc.perform(get(PRODUCTS_URL + "/{id}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Test Product"))
                    .andExpect(jsonPath("$.handle").value("test-product"))
                    .andExpect(jsonPath("$.status").value("DRAFT"));
        }

        @Test
        void shouldReturnSnakeCaseFields_whenProductRetrieved() throws Exception {
            // given
            UUID productId = UUID.randomUUID();
            when(productService.getProductById(productId)).thenReturn(sampleResponse());

            // when / then
            mockMvc.perform(get(PRODUCTS_URL + "/{id}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.is_giftcard").exists())
                    .andExpect(jsonPath("$.external_id").exists())
                    .andExpect(jsonPath("$.created_at").exists())
                    .andExpect(jsonPath("$.updated_at").exists());
        }
    }

    @Nested
    class GetProductByIdFailureCases {

        @Test
        void shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
            // given
            UUID unknownId = UUID.randomUUID();
            when(productService.getProductById(unknownId))
                    .thenThrow(new ResourceNotFoundException("Product", unknownId));

            // when / then
            mockMvc.perform(get(PRODUCTS_URL + "/{id}", unknownId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error_code").value("RESOURCE_NOT_FOUND"))
                    .andExpect(jsonPath("$.detail").value("Product not found with identifier: " + unknownId));
        }

        @Test
        void shouldReturnBadRequest_whenInvalidUuidFormat() throws Exception {
            // when / then
            mockMvc.perform(get(PRODUCTS_URL + "/not-a-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("INVALID_PARAMETER"));
        }
    }

    @Nested
    class ListProductsSuccessCases {

        @Test
        void shouldReturnOk_whenListingWithNoParams() throws Exception {
            // given
            PagedProductResponse pagedResponse = new PagedProductResponse(
                    List.of(sampleResponse()), 0, 20, 1, 1);
            when(productService.listProducts(isNull(), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // when / then
            mockMvc.perform(get(PRODUCTS_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.total_elements").value(1))
                    .andExpect(jsonPath("$.total_pages").value(1));
        }

        @Test
        void shouldReturnOk_withPaginationParams() throws Exception {
            // given
            PagedProductResponse pagedResponse = new PagedProductResponse(
                    List.of(sampleResponse()), 1, 10, 25, 3);
            when(productService.listProducts(isNull(), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // when / then
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.total_elements").value(25))
                    .andExpect(jsonPath("$.total_pages").value(3));
        }

        @Test
        void shouldReturnOk_withStatusFilter() throws Exception {
            // given
            PagedProductResponse pagedResponse = new PagedProductResponse(
                    List.of(sampleResponse()), 0, 20, 1, 1);
            when(productService.listProducts(eq("PUBLISHED"), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // when / then
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("status", "PUBLISHED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());

            verify(productService).listProducts(eq("PUBLISHED"), any(Pageable.class));
        }

        @Test
        void shouldReturnOk_withEmptyContent() throws Exception {
            // given
            PagedProductResponse pagedResponse = new PagedProductResponse(
                    List.of(), 0, 20, 0, 0);
            when(productService.listProducts(isNull(), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // when / then
            mockMvc.perform(get(PRODUCTS_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.total_elements").value(0))
                    .andExpect(jsonPath("$.total_pages").value(0));
        }

        @Test
        void shouldReturnSnakeCaseResponseFields_inPagedResponse() throws Exception {
            // given
            PagedProductResponse pagedResponse = new PagedProductResponse(
                    List.of(sampleResponse()), 0, 20, 1, 1);
            when(productService.listProducts(isNull(), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            // when / then
            mockMvc.perform(get(PRODUCTS_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total_elements").exists())
                    .andExpect(jsonPath("$.total_pages").exists())
                    .andExpect(jsonPath("$.content[0].is_giftcard").exists())
                    .andExpect(jsonPath("$.content[0].external_id").exists())
                    .andExpect(jsonPath("$.content[0].created_at").exists())
                    .andExpect(jsonPath("$.content[0].updated_at").exists());
        }
    }

    @Nested
    class ListProductsFailureCases {

        @Test
        void shouldReturnConflict_whenInvalidStatusFilter() throws Exception {
            // given
            when(productService.listProducts(eq("INVALID"), any(Pageable.class)))
                    .thenThrow(new BusinessRuleException("INVALID_STATUS",
                            "Invalid product status: INVALID. Valid statuses are: DRAFT, PUBLISHED, PROPOSED, REJECTED"));

            // when / then
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("status", "INVALID"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("INVALID_STATUS"));
        }
    }

    @Nested
    class UpdateProductSuccessCases {

        @Test
        void shouldReturnOk_whenUpdatingTitle() throws Exception {
            // given
            UUID productId = UUID.randomUUID();
            ProductResponse updatedResponse = new ProductResponse(
                    productId, "Updated Title", "test-product", "DRAFT",
                    null, null, false, true,
                    null, null, null, null, null, null,
                    Instant.now(), Instant.now());
            when(productService.updateProduct(eq(productId), any(UpdateProductRequest.class)))
                    .thenReturn(updatedResponse);

            String requestJson = """
                    {
                        "title": "Updated Title"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title"));
        }

        @Test
        void shouldReturnOk_whenUpdatingMultipleFields() throws Exception {
            // given
            UUID productId = UUID.randomUUID();
            when(productService.updateProduct(eq(productId), any(UpdateProductRequest.class)))
                    .thenReturn(sampleResponse());

            String requestJson = """
                    {
                        "title": "Updated Product",
                        "handle": "updated-product",
                        "status": "PUBLISHED",
                        "description": "Updated description"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").exists());
        }

        @Test
        void shouldReturnOk_whenEmptyBody() throws Exception {
            // given
            UUID productId = UUID.randomUUID();
            when(productService.updateProduct(eq(productId), any(UpdateProductRequest.class)))
                    .thenReturn(sampleResponse());

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnSnakeCaseFields_onUpdate() throws Exception {
            // given
            UUID productId = UUID.randomUUID();
            when(productService.updateProduct(eq(productId), any(UpdateProductRequest.class)))
                    .thenReturn(sampleResponse());

            String requestJson = """
                    {
                        "title": "Updated"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.is_giftcard").exists())
                    .andExpect(jsonPath("$.external_id").exists())
                    .andExpect(jsonPath("$.created_at").exists())
                    .andExpect(jsonPath("$.updated_at").exists());
        }
    }

    @Nested
    class UpdateProductFailureCases {

        @Test
        void shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
            // given
            UUID unknownId = UUID.randomUUID();
            when(productService.updateProduct(eq(unknownId), any(UpdateProductRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Product", unknownId));

            String requestJson = """
                    {
                        "title": "New Title"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", unknownId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error_code").value("RESOURCE_NOT_FOUND"));
        }

        @Test
        void shouldReturnConflict_whenDuplicateHandle() throws Exception {
            // given
            UUID productId = UUID.randomUUID();
            when(productService.updateProduct(eq(productId), any(UpdateProductRequest.class)))
                    .thenThrow(new BusinessRuleException("DUPLICATE_HANDLE",
                            "A product with handle 'taken' already exists"));

            String requestJson = """
                    {
                        "handle": "taken"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("DUPLICATE_HANDLE"));
        }

        @Test
        void shouldReturnConflict_whenInvalidStatus() throws Exception {
            // given
            UUID productId = UUID.randomUUID();
            when(productService.updateProduct(eq(productId), any(UpdateProductRequest.class)))
                    .thenThrow(new BusinessRuleException("INVALID_STATUS",
                            "Invalid product status: BOGUS"));

            String requestJson = """
                    {
                        "status": "BOGUS"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("INVALID_STATUS"));
        }

        @Test
        void shouldReturnConflict_whenBlankTitle() throws Exception {
            // given
            UUID productId = UUID.randomUUID();
            when(productService.updateProduct(eq(productId), any(UpdateProductRequest.class)))
                    .thenThrow(new BusinessRuleException("BLANK_TITLE",
                            "Product title must not be blank"));

            String requestJson = """
                    {
                        "title": "   "
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("BLANK_TITLE"));
        }

        @Test
        void shouldReturnBadRequest_whenInvalidUuidFormat() throws Exception {
            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/not-a-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\": \"Test\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("INVALID_PARAMETER"));
        }
    }
}
