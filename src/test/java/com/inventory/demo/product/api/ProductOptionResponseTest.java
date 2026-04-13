package com.inventory.demo.product.api;

import com.inventory.demo.product.domain.Product;
import com.inventory.demo.product.domain.ProductOption;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductOptionResponseTest {

    @Nested
    class FromEntityTests {

        @Test
        void shouldMapOptionWithValues() {
            // given
            Product product = Product.create("T-Shirt", "t-shirt");
            ProductOption option = product.addOption("Size");
            option.addValue("S");
            option.addValue("M");
            option.addValue("L");

            // when
            ProductOptionResponse response = ProductOptionResponse.fromEntity(option);

            // then
            assertThat(response.title()).isEqualTo("Size");
            assertThat(response.values()).containsExactly("S", "M", "L");
            assertThat(response.createdAt()).isNotNull();
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        void shouldMapOptionWithNoValues() {
            // given
            Product product = Product.create("T-Shirt", "t-shirt");
            ProductOption option = product.addOption("Color");

            // when
            ProductOptionResponse response = ProductOptionResponse.fromEntity(option);

            // then
            assertThat(response.title()).isEqualTo("Color");
            assertThat(response.values()).isEmpty();
        }
    }

    @Nested
    class DefensiveCopyTests {

        @Test
        void shouldReturnImmutableValuesList() {
            // given
            ProductOptionResponse response = new ProductOptionResponse(
                    null, "Size", List.of("S", "M"), null, null);

            // then
            assertThat(response.values()).hasSize(2);
        }
    }
}
