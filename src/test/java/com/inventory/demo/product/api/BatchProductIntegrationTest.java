package com.inventory.demo.product.api;

import com.fasterxml.jackson.databind.JsonNode;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BatchProductIntegrationTest {

    private static final String BATCH_URL = "/api/v1/products/batch";
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
        jdbcTemplate.execute("DELETE FROM variant_option_values");
        jdbcTemplate.execute("DELETE FROM product_option_values");
        jdbcTemplate.execute("DELETE FROM product_variants");
        jdbcTemplate.execute("DELETE FROM product_options");
        jdbcTemplate.execute("DELETE FROM products");
    }

    private String createProductAndReturnId(String title) throws Exception {
        String requestJson = """
                {
                    "title": "%s"
                }
                """.formatted(title);

        MvcResult result = mockMvc.perform(post(PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asText();
    }

    @Nested
    class BatchCreateCases {

        @Test
        void shouldBatchCreateMultipleProducts() throws Exception {
            // given
            String requestJson = """
                    {
                        "create": [
                            {"title": "Batch Product A"},
                            {"title": "Batch Product B"}
                        ]
                    }
                    """;

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.created").isArray())
                    .andExpect(jsonPath("$.created.length()").value(2))
                    .andExpect(jsonPath("$.created[0].success").value(true))
                    .andExpect(jsonPath("$.created[0].product.title").value("Batch Product A"))
                    .andExpect(jsonPath("$.created[1].success").value(true))
                    .andExpect(jsonPath("$.created[1].product.title").value("Batch Product B"))
                    .andExpect(jsonPath("$.updated").isEmpty())
                    .andExpect(jsonPath("$.deleted").isEmpty());

            assertThat(productRepository.count()).isEqualTo(2);
        }

        @Test
        void shouldHandlePartialCreateFailure_whenDuplicateHandle() throws Exception {
            // given — first create a product
            createProductAndReturnId("Existing Product");

            String requestJson = """
                    {
                        "create": [
                            {"title": "New Product"},
                            {"title": "Existing Product"}
                        ]
                    }
                    """;

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.created[0].success").value(true))
                    .andExpect(jsonPath("$.created[1].success").value(false))
                    .andExpect(jsonPath("$.created[1].error_code").value("DUPLICATE_HANDLE"));
        }
    }

    @Nested
    class BatchUpdateCases {

        @Test
        void shouldBatchUpdateMultipleProducts() throws Exception {
            // given
            String id1 = createProductAndReturnId("Product One");
            String id2 = createProductAndReturnId("Product Two");

            String requestJson = """
                    {
                        "update": [
                            {"id": "%s", "data": {"title": "Updated One"}},
                            {"id": "%s", "data": {"title": "Updated Two"}}
                        ]
                    }
                    """.formatted(id1, id2);

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.updated.length()").value(2))
                    .andExpect(jsonPath("$.updated[0].success").value(true))
                    .andExpect(jsonPath("$.updated[0].product.title").value("Updated One"))
                    .andExpect(jsonPath("$.updated[1].success").value(true))
                    .andExpect(jsonPath("$.updated[1].product.title").value("Updated Two"));
        }

        @Test
        void shouldHandleUpdateNotFound() throws Exception {
            // given
            String existingId = createProductAndReturnId("Existing Product");
            String missingId = java.util.UUID.randomUUID().toString();

            String requestJson = """
                    {
                        "update": [
                            {"id": "%s", "data": {"title": "Updated Existing"}},
                            {"id": "%s", "data": {"title": "Update Missing"}}
                        ]
                    }
                    """.formatted(existingId, missingId);

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.updated[0].success").value(true))
                    .andExpect(jsonPath("$.updated[1].success").value(false))
                    .andExpect(jsonPath("$.updated[1].error_code").value("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    class BatchDeleteCases {

        @Test
        void shouldBatchDeleteMultipleProducts() throws Exception {
            // given
            String id1 = createProductAndReturnId("Delete Product A");
            String id2 = createProductAndReturnId("Delete Product B");

            String requestJson = """
                    {
                        "delete": ["%s", "%s"]
                    }
                    """.formatted(id1, id2);

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deleted.length()").value(2))
                    .andExpect(jsonPath("$.deleted[0].success").value(true))
                    .andExpect(jsonPath("$.deleted[1].success").value(true));

            // soft-deleted products should not be visible
            assertThat(productRepository.count()).isZero();
        }

        @Test
        void shouldHandleDeleteNotFound() throws Exception {
            // given
            String missingId = java.util.UUID.randomUUID().toString();

            String requestJson = """
                    {
                        "delete": ["%s"]
                    }
                    """.formatted(missingId);

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deleted[0].success").value(false))
                    .andExpect(jsonPath("$.deleted[0].error_code").value("RESOURCE_NOT_FOUND"));
        }
    }

    @Nested
    class MixedBatchCases {

        @Test
        void shouldExecuteMixedBatchOperations() throws Exception {
            // given
            String existingId = createProductAndReturnId("Existing For Update");
            String deleteId = createProductAndReturnId("Existing For Delete");

            String requestJson = """
                    {
                        "create": [{"title": "Brand New Product"}],
                        "update": [{"id": "%s", "data": {"title": "Updated Product"}}],
                        "delete": ["%s"]
                    }
                    """.formatted(existingId, deleteId);

            // when / then
            mockMvc.perform(post(BATCH_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.created[0].success").value(true))
                    .andExpect(jsonPath("$.created[0].product.title").value("Brand New Product"))
                    .andExpect(jsonPath("$.updated[0].success").value(true))
                    .andExpect(jsonPath("$.updated[0].product.title").value("Updated Product"))
                    .andExpect(jsonPath("$.deleted[0].success").value(true));

            // 1 original + 1 batch-created - 1 deleted = 1 visible (the updated one is still visible;
            // the "Existing For Update" was updated, "Brand New Product" was created, "Existing For Delete" was soft-deleted)
            assertThat(productRepository.count()).isEqualTo(2);
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void shouldReturnOk_whenEmptyArraysProvided() throws Exception {
            // given
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
        void shouldReturnBadRequest_whenCreateItemMissingTitle() throws Exception {
            // given
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
