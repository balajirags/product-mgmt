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
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Nested
    class GetProductByIdCases {

        @Test
        void shouldReturnProduct_whenExists() throws Exception {
            // given — create a product first
            String createJson = """
                    {
                        "title": "Retrievable Product",
                        "description": "A product to retrieve",
                        "subtitle": "Fetch me",
                        "status": "PUBLISHED",
                        "weight": 2.5,
                        "height": 10.0,
                        "width": 5.0,
                        "length": 3.0,
                        "metadata": "{\\"color\\":\\"blue\\"}",
                        "external_id": "EXT-GET-001"
                    }
                    """;
            MvcResult createResult = mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String productId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asText();

            // when / then
            mockMvc.perform(get(PRODUCTS_URL + "/{id}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(productId))
                    .andExpect(jsonPath("$.title").value("Retrievable Product"))
                    .andExpect(jsonPath("$.handle").value("retrievable-product"))
                    .andExpect(jsonPath("$.status").value("PUBLISHED"))
                    .andExpect(jsonPath("$.description").value("A product to retrieve"))
                    .andExpect(jsonPath("$.subtitle").value("Fetch me"))
                    .andExpect(jsonPath("$.is_giftcard").value(false))
                    .andExpect(jsonPath("$.discountable").value(true))
                    .andExpect(jsonPath("$.weight").value(2.5))
                    .andExpect(jsonPath("$.height").value(10.0))
                    .andExpect(jsonPath("$.width").value(5.0))
                    .andExpect(jsonPath("$.length").value(3.0))
                    .andExpect(jsonPath("$.metadata").value("{\"color\":\"blue\"}"))
                    .andExpect(jsonPath("$.external_id").value("EXT-GET-001"))
                    .andExpect(jsonPath("$.created_at").exists())
                    .andExpect(jsonPath("$.updated_at").exists());
        }

        @Test
        void shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
            // given
            UUID unknownId = UUID.randomUUID();

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
    class ListProductsCases {

        @Test
        void shouldReturnEmptyList_whenNoProductsExist() throws Exception {
            // when / then
            mockMvc.perform(get(PRODUCTS_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.total_elements").value(0))
                    .andExpect(jsonPath("$.total_pages").value(0))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        void shouldReturnAllProducts_whenNoFilter() throws Exception {
            // given
            createProduct("Product A");
            createProduct("Product B");
            createProduct("Product C");

            // when / then
            mockMvc.perform(get(PRODUCTS_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(3))
                    .andExpect(jsonPath("$.total_elements").value(3))
                    .andExpect(jsonPath("$.total_pages").value(1));
        }

        @Test
        void shouldPaginate_whenPageAndSizeProvided() throws Exception {
            // given
            for (int i = 0; i < 5; i++) {
                createProduct("Paged Product " + i);
            }

            // when / then
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("page", "0")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.total_elements").value(5))
                    .andExpect(jsonPath("$.total_pages").value(3))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(2));
        }

        @Test
        void shouldReturnSecondPage() throws Exception {
            // given
            for (int i = 0; i < 5; i++) {
                createProduct("Page Product " + i);
            }

            // when / then
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("page", "1")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.page").value(1));
        }

        @Test
        void shouldFilterByStatus() throws Exception {
            // given
            createProduct("Draft Product");
            createProductWithStatus("Published Product", "PUBLISHED");

            // when / then
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("status", "PUBLISHED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].status").value("PUBLISHED"))
                    .andExpect(jsonPath("$.total_elements").value(1));
        }

        @Test
        void shouldFilterByStatusCaseInsensitive() throws Exception {
            // given
            createProductWithStatus("Published Product", "PUBLISHED");

            // when / then
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("status", "published"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].status").value("PUBLISHED"));
        }

        @Test
        void shouldReturnConflict_whenInvalidStatusFilter() throws Exception {
            // when / then
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("status", "INVALID"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("INVALID_STATUS"));
        }

        @Test
        void shouldSortByCreatedAtDescByDefault() throws Exception {
            // given
            createProduct("First Product");
            createProduct("Second Product");

            // when / then
            mockMvc.perform(get(PRODUCTS_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].title").value("Second Product"))
                    .andExpect(jsonPath("$.content[1].title").value("First Product"));
        }

        @Test
        void shouldSortByExplicitParam() throws Exception {
            // given
            createProduct("Banana Product");
            createProduct("Apple Product");

            // when / then
            mockMvc.perform(get(PRODUCTS_URL)
                            .param("sort", "title,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("Apple Product"))
                    .andExpect(jsonPath("$.content[1].title").value("Banana Product"));
        }

        @Test
        void shouldReturnPageResponseStructure() throws Exception {
            // given
            createProduct("Structure Test");

            // when / then
            mockMvc.perform(get(PRODUCTS_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.page").isNumber())
                    .andExpect(jsonPath("$.size").isNumber())
                    .andExpect(jsonPath("$.total_elements").isNumber())
                    .andExpect(jsonPath("$.total_pages").isNumber())
                    .andExpect(jsonPath("$.content[0].is_giftcard").exists())
                    .andExpect(jsonPath("$.content[0].created_at").exists())
                    .andExpect(jsonPath("$.content[0].updated_at").exists());
        }
    }

    private void createProduct(String title) throws Exception {
        String requestJson = String.format("""
                {
                    "title": "%s"
                }
                """, title);
        mockMvc.perform(post(PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());
    }

    private void createProductWithStatus(String title, String status) throws Exception {
        String requestJson = String.format("""
                {
                    "title": "%s",
                    "status": "%s"
                }
                """, title, status);
        mockMvc.perform(post(PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());
    }
}
