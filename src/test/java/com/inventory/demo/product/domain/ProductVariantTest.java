package com.inventory.demo.product.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ProductVariantTest {

    private static Product sampleProduct() {
        return Product.create("T-Shirt", "t-shirt");
    }

    @Nested
    class FactoryMethodTests {

        @Test
        void shouldCreateVariantWithProductAndTitle() {
            // given
            Product product = sampleProduct();

            // when
            ProductVariant variant = ProductVariant.create(product, "S / Red");

            // then
            assertThat(variant.getProduct()).isSameAs(product);
            assertThat(variant.getTitle()).isEqualTo("S / Red");
        }

        @Test
        void shouldSetTimestampsOnCreation() {
            // when
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // then
            assertThat(variant.getCreatedAt()).isNotNull();
            assertThat(variant.getUpdatedAt()).isNotNull();
            assertThat(variant.getCreatedAt()).isCloseTo(
                    variant.getUpdatedAt(), within(1, ChronoUnit.SECONDS));
        }

        @Test
        void shouldNotHaveIdBeforePersistence() {
            // when
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // then
            assertThat(variant.getId()).isNull();
        }

        @Test
        void shouldDefaultManageInventoryToFalse() {
            // when
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // then
            assertThat(variant.isManageInventory()).isFalse();
        }

        @Test
        void shouldDefaultAllowBackorderToFalse() {
            // when
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // then
            assertThat(variant.isAllowBackorder()).isFalse();
        }

        @Test
        void shouldHaveNullDeletedAtByDefault() {
            // when
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // then
            assertThat(variant.getDeletedAt()).isNull();
        }

        @Test
        void shouldHaveEmptyOptionValuesByDefault() {
            // when
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // then
            assertThat(variant.getOptionValues()).isEmpty();
        }

        @Test
        void shouldHaveNullSkuAndBarcodeByDefault() {
            // when
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // then
            assertThat(variant.getSku()).isNull();
            assertThat(variant.getBarcode()).isNull();
        }
    }

    @Nested
    class SkuAndBarcodeTests {

        @Test
        void shouldAssignSku() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // when
            variant.assignSku("SKU-001");

            // then
            assertThat(variant.getSku()).isEqualTo("SKU-001");
        }

        @Test
        void shouldAssignBarcode() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // when
            variant.assignBarcode("1234567890");

            // then
            assertThat(variant.getBarcode()).isEqualTo("1234567890");
        }

        @Test
        void shouldUpdateTimestampOnSkuAssignment() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");
            Instant beforeUpdate = variant.getUpdatedAt();

            // when
            variant.assignSku("SKU-002");

            // then
            assertThat(variant.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }

        @Test
        void shouldUpdateTimestampOnBarcodeAssignment() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");
            Instant beforeUpdate = variant.getUpdatedAt();

            // when
            variant.assignBarcode("BC-001");

            // then
            assertThat(variant.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }
    }

    @Nested
    class DimensionTests {

        @Test
        void shouldApplyDimensions() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // when
            variant.applyDimensions(
                    new BigDecimal("1.5"),
                    new BigDecimal("10.0"),
                    new BigDecimal("5.0"),
                    new BigDecimal("3.0"));

            // then
            assertThat(variant.getWeight()).isEqualByComparingTo(new BigDecimal("1.5"));
            assertThat(variant.getHeight()).isEqualByComparingTo(new BigDecimal("10.0"));
            assertThat(variant.getWidth()).isEqualByComparingTo(new BigDecimal("5.0"));
            assertThat(variant.getLength()).isEqualByComparingTo(new BigDecimal("3.0"));
        }

        @Test
        void shouldUpdateTimestampOnApplyDimensions() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");
            Instant beforeUpdate = variant.getUpdatedAt();

            // when
            variant.applyDimensions(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);

            // then
            assertThat(variant.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }

        @Test
        void shouldHaveNullDimensionsByDefault() {
            // when
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // then
            assertThat(variant.getWeight()).isNull();
            assertThat(variant.getHeight()).isNull();
            assertThat(variant.getWidth()).isNull();
            assertThat(variant.getLength()).isNull();
        }
    }

    @Nested
    class FlagTests {

        @Test
        void shouldSetManageInventory() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // when
            variant.setManageInventory(true);

            // then
            assertThat(variant.isManageInventory()).isTrue();
        }

        @Test
        void shouldSetAllowBackorder() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // when
            variant.setAllowBackorder(true);

            // then
            assertThat(variant.isAllowBackorder()).isTrue();
        }

        @Test
        void shouldUpdateTimestampOnManageInventoryChange() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");
            Instant beforeUpdate = variant.getUpdatedAt();

            // when
            variant.setManageInventory(true);

            // then
            assertThat(variant.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }

        @Test
        void shouldUpdateTimestampOnAllowBackorderChange() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");
            Instant beforeUpdate = variant.getUpdatedAt();

            // when
            variant.setAllowBackorder(true);

            // then
            assertThat(variant.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }
    }

    @Nested
    class OptionValueTests {

        @Test
        void shouldAddOptionValue() {
            // given
            Product product = sampleProduct();
            ProductOption option = product.addOption("Size");
            ProductOptionValue value = option.addValue("S");
            ProductVariant variant = ProductVariant.create(product, "Small");

            // when
            variant.addOptionValue(value);

            // then
            assertThat(variant.getOptionValues()).containsExactly(value);
        }

        @Test
        void shouldReplaceOptionValues() {
            // given
            Product product = sampleProduct();
            ProductOption option = product.addOption("Size");
            ProductOptionValue small = option.addValue("S");
            ProductOptionValue medium = option.addValue("M");
            ProductVariant variant = ProductVariant.create(product, "Small");
            variant.addOptionValue(small);

            // when
            variant.replaceOptionValues(List.of(medium));

            // then
            assertThat(variant.getOptionValues()).containsExactly(medium);
        }

        @Test
        void shouldReturnUnmodifiableOptionValues() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // then
            assertThat(variant.getOptionValues()).isUnmodifiable();
        }

        @Test
        void shouldUpdateTimestampOnAddOptionValue() {
            // given
            Product product = sampleProduct();
            ProductOption option = product.addOption("Color");
            ProductOptionValue value = option.addValue("Red");
            ProductVariant variant = ProductVariant.create(product, "Red");
            Instant beforeUpdate = variant.getUpdatedAt();

            // when
            variant.addOptionValue(value);

            // then
            assertThat(variant.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }
    }

    @Nested
    class SoftDeleteTests {

        @Test
        void shouldSetDeletedAtOnSoftDelete() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // when
            variant.softDelete();

            // then
            assertThat(variant.getDeletedAt()).isNotNull();
        }

        @Test
        void shouldUpdateTimestampOnSoftDelete() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");
            Instant beforeDelete = variant.getUpdatedAt();

            // when
            variant.softDelete();

            // then
            assertThat(variant.getUpdatedAt()).isAfterOrEqualTo(beforeDelete);
        }
    }

    @Nested
    class TitleTests {

        @Test
        void shouldGenerateTitleFromOptionValues() {
            // given
            Product product = sampleProduct();
            ProductOption sizeOption = product.addOption("Size");
            ProductOptionValue small = sizeOption.addValue("S");
            ProductOption colorOption = product.addOption("Color");
            ProductOptionValue red = colorOption.addValue("Red");

            ProductVariant variant = ProductVariant.create(product, "Temp");
            variant.addOptionValue(small);
            variant.addOptionValue(red);

            // when
            String generated = variant.generateTitle();

            // then
            assertThat(generated).isEqualTo("S / Red");
        }

        @Test
        void shouldGenerateEmptyTitleWhenNoOptionValues() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Default");

            // when
            String generated = variant.generateTitle();

            // then
            assertThat(generated).isEmpty();
        }

        @Test
        void shouldUpdateTitle() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Original");

            // when
            variant.updateTitle("Updated Title");

            // then
            assertThat(variant.getTitle()).isEqualTo("Updated Title");
        }

        @Test
        void shouldUpdateTimestampOnTitleUpdate() {
            // given
            ProductVariant variant = ProductVariant.create(sampleProduct(), "Original");
            Instant beforeUpdate = variant.getUpdatedAt();

            // when
            variant.updateTitle("New Title");

            // then
            assertThat(variant.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
        }
    }
}
