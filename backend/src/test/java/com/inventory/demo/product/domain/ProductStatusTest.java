package com.inventory.demo.product.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProductStatusTest {

    @Test
    void shouldHaveFourValues() {
        assertThat(ProductStatus.values()).hasSize(4);
    }

    @ParameterizedTest
    @EnumSource(ProductStatus.class)
    void shouldResolveFromName(ProductStatus status) {
        // when
        ProductStatus resolved = ProductStatus.valueOf(status.name());

        // then
        assertThat(resolved).isEqualTo(status);
    }

    @Test
    void shouldContainExpectedValues() {
        assertThat(ProductStatus.values()).containsExactly(
                ProductStatus.DRAFT,
                ProductStatus.PUBLISHED,
                ProductStatus.PROPOSED,
                ProductStatus.REJECTED
        );
    }
}
