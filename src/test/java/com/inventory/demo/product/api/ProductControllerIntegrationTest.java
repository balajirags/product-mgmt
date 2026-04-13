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
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM product_option_values");
        jdbcTemplate.execute("DELETE FROM product_options");
        jdbcTemplate.execute("DELETE FROM products");
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

    private String createProductAndReturnId(String title) throws Exception {
        String requestJson = String.format("""
                {
                    "title": "%s"
                }
                """, title);
        MvcResult result = mockMvc.perform(post(PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Nested
    class UpdateProductCases {

        @Test
        void shouldUpdateTitle() throws Exception {
            // given
            String productId = createProductAndReturnId("Original Title");

            String updateJson = """
                    {
                        "title": "Updated Title"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(productId))
                    .andExpect(jsonPath("$.title").value("Updated Title"))
                    .andExpect(jsonPath("$.handle").value("original-title"));
        }

        @Test
        void shouldUpdateMultipleFields() throws Exception {
            // given
            String productId = createProductAndReturnId("Multi Update");

            String updateJson = """
                    {
                        "title": "New Title",
                        "description": "New description",
                        "subtitle": "New subtitle",
                        "status": "PUBLISHED"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("New Title"))
                    .andExpect(jsonPath("$.description").value("New description"))
                    .andExpect(jsonPath("$.subtitle").value("New subtitle"))
                    .andExpect(jsonPath("$.status").value("PUBLISHED"));
        }

        @Test
        void shouldUpdateHandle() throws Exception {
            // given
            String productId = createProductAndReturnId("Handle Update Test");

            String updateJson = """
                    {
                        "handle": "new-custom-handle"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.handle").value("new-custom-handle"));
        }

        @Test
        void shouldPreserveUnchangedFields() throws Exception {
            // given — create product with all fields
            String createJson = """
                    {
                        "title": "Full Product",
                        "description": "Original description",
                        "subtitle": "Original subtitle",
                        "weight": 1.5,
                        "metadata": "{\\"key\\":\\"value\\"}"
                    }
                    """;
            MvcResult createResult = mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();
            String productId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asText();

            // when — update only title
            String updateJson = """
                    {
                        "title": "Updated Title Only"
                    }
                    """;

            // then — other fields preserved
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Title Only"))
                    .andExpect(jsonPath("$.description").value("Original description"))
                    .andExpect(jsonPath("$.subtitle").value("Original subtitle"))
                    .andExpect(jsonPath("$.weight").value(1.5))
                    .andExpect(jsonPath("$.metadata").value("{\"key\":\"value\"}"));
        }

        @Test
        void shouldReturnOk_whenEmptyBody() throws Exception {
            // given
            String productId = createProductAndReturnId("Empty Body Test");

            // when / then — no-op update
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Empty Body Test"));
        }

        @Test
        void shouldRefreshUpdatedAtTimestamp() throws Exception {
            // given
            String createJson = """
                    {
                        "title": "Timestamp Test"
                    }
                    """;
            MvcResult createResult = mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();
            String productId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asText();
            String originalUpdatedAt = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("updated_at").asText();

            // when
            String updateJson = """
                    {
                        "title": "Updated Timestamp"
                    }
                    """;
            MvcResult updateResult = mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andReturn();
            String newUpdatedAt = objectMapper.readTree(
                    updateResult.getResponse().getContentAsString()).get("updated_at").asText();

            // then
            assertThat(newUpdatedAt).isNotEqualTo(originalUpdatedAt);
        }

        @Test
        void shouldUpdateStatusLifecycle() throws Exception {
            // given — create DRAFT product
            String productId = createProductAndReturnId("Status Lifecycle");

            // when — transition to PUBLISHED
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"PUBLISHED\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PUBLISHED"));

            // then — verify via GET
            mockMvc.perform(get(PRODUCTS_URL + "/{id}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PUBLISHED"));
        }

        @Test
        void shouldAllowSameHandleOnSameProduct() throws Exception {
            // given
            String createJson = """
                    {
                        "title": "Same Handle",
                        "handle": "my-handle"
                    }
                    """;
            MvcResult createResult = mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();
            String productId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asText();

            // when — update with same handle (should not conflict with itself)
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"handle\": \"my-handle\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.handle").value("my-handle"));
        }
    }

    @Nested
    class UpdateProductFailureCases {

        @Test
        void shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
            // given
            UUID unknownId = UUID.randomUUID();

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", unknownId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\": \"New Title\"}"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error_code").value("RESOURCE_NOT_FOUND"));
        }

        @Test
        void shouldReturnConflict_whenDuplicateHandle() throws Exception {
            // given — create two products
            String firstId = createProductAndReturnId("First Product");
            String createJson = """
                    {
                        "title": "Second Product",
                        "handle": "taken-handle"
                    }
                    """;
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated());

            // when — try to set first product's handle to second's
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", firstId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"handle\": \"taken-handle\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("DUPLICATE_HANDLE"));
        }

        @Test
        void shouldReturnConflict_whenInvalidStatus() throws Exception {
            // given
            String productId = createProductAndReturnId("Invalid Status Test");

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"BOGUS\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("INVALID_STATUS"));
        }

        @Test
        void shouldReturnConflict_whenBlankTitle() throws Exception {
            // given
            String productId = createProductAndReturnId("Blank Title Test");

            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\": \"   \"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("BLANK_TITLE"));
        }

        @Test
        void shouldReturnBadRequest_whenInvalidUuid() throws Exception {
            // when / then
            mockMvc.perform(post(PRODUCTS_URL + "/not-a-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\": \"Test\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error_code").value("INVALID_PARAMETER"));
        }
    }

    @Nested
    class ProductOptionsCases {

        @Test
        void shouldCreateProductWithOptions() throws Exception {
            // given
            String requestJson = """
                    {
                        "title": "T-Shirt With Options",
                        "options": [
                            {
                                "title": "Size",
                                "values": ["S", "M", "L"]
                            },
                            {
                                "title": "Color",
                                "values": ["Red", "Blue"]
                            }
                        ]
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.options").isArray())
                    .andExpect(jsonPath("$.options.length()").value(2))
                    .andExpect(jsonPath("$.options[0].title").value("Size"))
                    .andExpect(jsonPath("$.options[0].values.length()").value(3))
                    .andExpect(jsonPath("$.options[0].values[0]").value("S"))
                    .andExpect(jsonPath("$.options[1].title").value("Color"))
                    .andExpect(jsonPath("$.options[1].values.length()").value(2));
        }

        @Test
        void shouldCreateProductWithoutOptions() throws Exception {
            // given
            String requestJson = """
                    {
                        "title": "Simple Product"
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.options").isArray())
                    .andExpect(jsonPath("$.options.length()").value(0));
        }

        @Test
        void shouldRejectDuplicateOptionTitles() throws Exception {
            // given
            String requestJson = """
                    {
                        "title": "Duplicate Options Product",
                        "options": [
                            {
                                "title": "Size",
                                "values": ["S", "M"]
                            },
                            {
                                "title": "Size",
                                "values": ["L", "XL"]
                            }
                        ]
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error_code").value("DUPLICATE_OPTION_TITLE"));
        }

        @Test
        void shouldRejectOptionWithBlankTitle() throws Exception {
            // given
            String requestJson = """
                    {
                        "title": "Bad Option Product",
                        "options": [
                            {
                                "title": "",
                                "values": ["S", "M"]
                            }
                        ]
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectOptionWithEmptyValues() throws Exception {
            // given
            String requestJson = """
                    {
                        "title": "Empty Values Product",
                        "options": [
                            {
                                "title": "Size",
                                "values": []
                            }
                        ]
                    }
                    """;

            // when / then
            mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnOptionsOnGetById() throws Exception {
            // given — create product with options
            String createJson = """
                    {
                        "title": "Get Options Test",
                        "options": [
                            {
                                "title": "Material",
                                "values": ["Cotton", "Polyester"]
                            }
                        ]
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
                    .andExpect(jsonPath("$.options").isArray())
                    .andExpect(jsonPath("$.options.length()").value(1))
                    .andExpect(jsonPath("$.options[0].title").value("Material"))
                    .andExpect(jsonPath("$.options[0].values[0]").value("Cotton"))
                    .andExpect(jsonPath("$.options[0].values[1]").value("Polyester"));
        }

        @Test
        void shouldUpdateProductOptions() throws Exception {
            // given — create product with initial options
            String createJson = """
                    {
                        "title": "Update Options Test",
                        "options": [
                            {
                                "title": "Size",
                                "values": ["S", "M"]
                            },
                            {
                                "title": "Color",
                                "values": ["Red"]
                            }
                        ]
                    }
                    """;

            MvcResult createResult = mockMvc.perform(post(PRODUCTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String productId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asText();

            // when — update: change Size values, remove Color, add Material
            String updateJson = """
                    {
                        "options": [
                            {
                                "title": "Size",
                                "values": ["L", "XL", "XXL"]
                            },
                            {
                                "title": "Material",
                                "values": ["Cotton", "Silk"]
                            }
                        ]
                    }
                    """;

            mockMvc.perform(post(PRODUCTS_URL + "/{id}", productId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.options.length()").value(2))
                    .andExpect(jsonPath("$.options[0].title").value("Size"))
                    .andExpect(jsonPath("$.options[0].values.length()").value(3))
                    .andExpect(jsonPath("$.options[0].values[0]").value("L"));
        }
    }
}
