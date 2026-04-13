package com.inventory.demo.product.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ProductOptionValueTest {

    private static ProductOption sampleOption() {
        return ProductOption.create(Product.create("T-Shirt", "t-shirt"), "Size");
    }

    @Nested
    class FactoryMethodTests {

        @Test
        void shouldCreateValueWithTextAndOption() {
            // given
            ProductOption option = sampleOption();

            // when
            ProductOptionValue value = ProductOptionValue.create(option, "Large");

            // then
            assertThat(value.getOption()).isSameAs(option);
            assertThat(value.getValue()).isEqualTo("Large");
        }

        @Test
        void shouldSetTimestampsOnCreation() {
            // when
            ProductOptionValue value = ProductOptionValue.create(sampleOption(), "Red");

            // then
            assertThat(value.getCreatedAt()).isNotNull();
            assertThat(value.getUpdatedAt()).isNotNull();
            assertThat(value.getCreatedAt()).isCloseTo(
                    value.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
        }

        @Test
        void shouldNotHaveIdBeforePersistence() {
            // when
            ProductOptionValue value = ProductOptionValue.create(sampleOption(), "XL");

            // then
            assertThat(value.getId()).isNull();
        }

        @Test
        void shouldHaveNullDeletedAtByDefault() {
            // when
            ProductOptionValue value = ProductOptionValue.create(sampleOption(), "S");

            // then
            assertThat(value.getDeletedAt()).isNull();
        }
    }

    @Nested
    class SoftDeleteTests {

        @Test
        void shouldSetDeletedAtOnSoftDelete() {
            // given
            ProductOptionValue value = ProductOptionValue.create(sampleOption(), "M");

            // when
            value.softDelete();

            // then
            assertThat(value.getDeletedAt()).isNotNull();
        }

        @Test
        void shouldUpdateTimestampOnSoftDelete() {
            // given
            ProductOptionValue value = ProductOptionValue.create(sampleOption(), "M");

            // when
            value.softDelete();

            // then
            assertThat(value.getUpdatedAt()).isNotNull();
        }
    }
}
