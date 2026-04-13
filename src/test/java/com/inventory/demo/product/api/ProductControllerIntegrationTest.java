package com.inventory.demo.product.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.demo.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    private static final String PRODUCTS_URL = "/api/v1/products";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Nested
    class CreateProductCases {

        @Test
        void shouldCreateProductWithTitleOnly() throws Exception {
            // given
            String requestJson = """
                    {
                        "title": "Integration Test Product"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.title").value("Integration Test Product"))
                    .andExpect(jsonPath("$.handle").value("integration-test-product"))
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.is_giftcard").value(false))
                    .andExpect(jsonPath("$.discountable").value(true))
                    .andExpect(jsonPath("$.created_at").exists())
                    .andExpect(jsonPath("$.updated_at").exists());

            assertThat(productRepository.count()).isEqualTo(1);
        }

        @Test
        void shouldCreateProductWithExplicitHandle() throws Exception {
            // given
            String requestJson = """
                    {
                        "title": "My Product",
                        "handle": "my-custom-handle"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.handle").value("my-custom-handle"));
        }

        @Test
        void shouldCreateProductWithExplicitStatus() throws Exception {
            // given
            String requestJson = """
                    {
                        "title": "Published Product",
                        "status": "PUBLISHED"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("PUBLISHED"));
        }

        @Test
        void shouldCreateProductWithDimensions() throws Exception {
            // given
            String requestJson = """
                    {
                        "title": "Dimensional Product",
                        "weight": 2.5,
                        "height": 10.0,
                        "width": 5.0,
                        "length": 3.0
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.weight").value(2.5))
                    .andExpect(jsonPath("$.height").value(10.0))
                    .andExpect(jsonPath("$.width").value(5.0))
                    .andExpect(jsonPath("$.length").value(3.0));
        }

        @Test
        void shouldCreateProductWithMetadata() throws Exception {
            // given
            String requestJson = """
                    {
                        "title": "Metadata Product",
                        "metadata": "{\\"color\\":\\"red\\"}"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.metadata").value("{\"color\":\"red\"}"));
        }
    }

    @Nested
    class ValidationFailureCases {

        @Test
        void shouldReturnBadRequest_whenTitleMissing() throws Exception {
            // given
            String requestJson = """
                    {
                        "handle": "no-title"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("VALIDATION_ERROR"));
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
        }

        @Test
        void shouldReturnConflict_whenInvalidStatus() throws Exception {
            // given
            String requestJson = """
                    {
                        "title": "Test Product",
                        "status": "INVALID_STATUS"
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
    class DuplicateHandleCases {

        @Test
        void shouldReturnConflict_whenDuplicateHandle() throws Exception {
            // given — create first product
            String firstRequest = """
                    {
                        "title": "First Product",
                        "handle": "unique-handle"
                    }
                    """;
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(firstRequest))
                    .andExpect(status().isCreated());

            // when — attempt duplicate
            String duplicateRequest = """
                    {
                        "title": "Second Product",
                        "handle": "unique-handle"
                    }
                    """;

            // then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(duplicateRequest))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("DUPLICATE_HANDLE"))
                    .andExpect(jsonPath("$.detail").value("A product with handle 'unique-handle' already exists"));
        }

        @Test
        void shouldReturnConflict_whenAutoGeneratedHandleClashes() throws Exception {
            // given — create first product with auto-generated handle
            String firstRequest = """
                    {
                        "title": "Same Title"
                    }
                    """;
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(firstRequest))
                    .andExpect(status().isCreated());

            // when — create second product with same title (same auto-generated handle)
            // then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(firstRequest))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("DUPLICATE_HANDLE"));
        }
    }
}
