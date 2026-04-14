package com.inventory.demo.product.api;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PagedProductResponseTest {

    private static ProductResponse sampleResponse() {
        return new ProductResponse(
                UUID.randomUUID(), "Test Product", "test-product", "DRAFT",
                "desc", "sub", false, true,
                new BigDecimal("1.0"), new BigDecimal("2.0"),
                new BigDecimal("3.0"), new BigDecimal("4.0"),
                null, null, null, Instant.now(), Instant.now(),
                List.of(), List.of(), List.of()
        );
    }

    @Test
    void shouldMapPageToPagedProductResponse() {
        // given
        ProductResponse response = sampleResponse();
        Page<ProductResponse> page = new PageImpl<>(
                List.of(response), PageRequest.of(0, 20), 1);

        // when
        PagedProductResponse result = PagedProductResponse.fromPage(page);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void shouldHandleEmptyPage() {
        // given
        Page<ProductResponse> page = new PageImpl<>(
                List.of(), PageRequest.of(0, 20), 0);

        // when
        PagedProductResponse result = PagedProductResponse.fromPage(page);

        // then
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void shouldReflectCorrectPaginationMetrics() {
        // given
        List<ProductResponse> items = List.of(sampleResponse(), sampleResponse());
        Page<ProductResponse> page = new PageImpl<>(
                items, PageRequest.of(1, 10), 25);

        // when
        PagedProductResponse result = PagedProductResponse.fromPage(page);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(25);
        assertThat(result.totalPages()).isEqualTo(3);
    }
}
