package com.inventory.demo.product.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class ProductOptionTest {

    private static Product sampleProduct() {
        return Product.create("T-Shirt", "t-shirt");
    }

    @Nested
    class FactoryMethodTests {

        @Test
        void shouldCreateOptionWithTitleAndProduct() {
            // given
            Product product = sampleProduct();

            // when
            ProductOption option = ProductOption.create(product, "Size");

            // then
            assertThat(option.getProduct()).isSameAs(product);
            assertThat(option.getTitle()).isEqualTo("Size");
            assertThat(option.getValues()).isEmpty();
        }

        @Test
        void shouldSetTimestampsOnCreation() {
            // when
            ProductOption option = ProductOption.create(sampleProduct(), "Color");

            // then
            assertThat(option.getCreatedAt()).isNotNull();
            assertThat(option.getUpdatedAt()).isNotNull();
            assertThat(option.getCreatedAt()).isCloseTo(
                    option.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
        }

        @Test
        void shouldNotHaveIdBeforePersistence() {
            // when
            ProductOption option = ProductOption.create(sampleProduct(), "Material");

            // then
            assertThat(option.getId()).isNull();
        }

        @Test
        void shouldHaveNullDeletedAtByDefault() {
            // when
            ProductOption option = ProductOption.create(sampleProduct(), "Style");

            // then
            assertThat(option.getDeletedAt()).isNull();
        }
    }

    @Nested
    class AddValueTests {

        @Test
        void shouldAddSingleValue() {
            // given
            ProductOption option = ProductOption.create(sampleProduct(), "Size");

            // when
            ProductOptionValue value = option.addValue("Small");

            // then
            assertThat(option.getValues()).hasSize(1);
            assertThat(value.getValue()).isEqualTo("Small");
            assertThat(value.getOption()).isSameAs(option);
        }

        @Test
        void shouldAddMultipleValues() {
            // given
            ProductOption option = ProductOption.create(sampleProduct(), "Size");

            // when
            option.addValue("S");
            option.addValue("M");
            option.addValue("L");

            // then
            assertThat(option.getValues()).hasSize(3);
            assertThat(option.getValues())
                    .extracting(ProductOptionValue::getValue)
                    .containsExactly("S", "M", "L");
        }

        @Test
        void shouldUpdateTimestampWhenAddingValue() {
            // given
            ProductOption option = ProductOption.create(sampleProduct(), "Size");

            // when
            option.addValue("XL");

            // then
            assertThat(option.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    class ReplaceValuesTests {

        @Test
        void shouldReplaceAllValues() {
            // given
            ProductOption option = ProductOption.create(sampleProduct(), "Color");
            option.addValue("Red");
            option.addValue("Blue");

            // when
            option.replaceValues(List.of("Green", "Yellow", "Purple"));

            // then
            assertThat(option.getValues()).hasSize(3);
            assertThat(option.getValues())
                    .extracting(ProductOptionValue::getValue)
                    .containsExactly("Green", "Yellow", "Purple");
        }

        @Test
        void shouldClearAllValuesWhenReplacingWithEmptyList() {
            // given
            ProductOption option = ProductOption.create(sampleProduct(), "Size");
            option.addValue("S");
            option.addValue("M");

            // when
            option.replaceValues(List.of());

            // then
            assertThat(option.getValues()).isEmpty();
        }
    }

    @Nested
    class SoftDeleteTests {

        @Test
        void shouldSetDeletedAtOnSoftDelete() {
            // given
            ProductOption option = ProductOption.create(sampleProduct(), "Size");

            // when
            option.softDelete();

            // then
            assertThat(option.getDeletedAt()).isNotNull();
        }

        @Test
        void shouldSoftDeleteAllChildValues() {
            // given
            ProductOption option = ProductOption.create(sampleProduct(), "Color");
            option.addValue("Red");
            option.addValue("Blue");

            // when
            option.softDelete();

            // then
            assertThat(option.getValues())
                    .allSatisfy(v -> assertThat(v.getDeletedAt()).isNotNull());
        }
    }

    @Nested
    class ImmutabilityTests {

        @Test
        void shouldReturnUnmodifiableValuesList() {
            // given
            ProductOption option = ProductOption.create(sampleProduct(), "Size");
            option.addValue("S");

            // when / then
            assertThatThrownBy(() -> option.getValues().add(
                    ProductOptionValue.create(option, "M")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
