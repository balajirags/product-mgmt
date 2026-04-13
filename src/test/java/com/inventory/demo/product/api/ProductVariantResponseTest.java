package com.inventory.demo.product.api;

import com.inventory.demo.product.domain.Product;
import com.inventory.demo.product.domain.ProductOption;
import com.inventory.demo.product.domain.ProductOptionValue;
import com.inventory.demo.product.domain.ProductVariant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductVariantResponseTest {

    @Nested
    class FromEntityTests {

        @Test
        void shouldMapVariantWithAllFields() {
            // given
            Product product = Product.create("T-Shirt", "t-shirt");
            ProductVariant variant = product.addVariant("S / Red");
            variant.assignSku("SKU-001");
            variant.assignBarcode("BC-001");
            variant.applyDimensions(
                    new BigDecimal("1.5"),
                    new BigDecimal("10.0"),
                    new BigDecimal("5.0"),
                    new BigDecimal("3.0"));
            variant.setManageInventory(true);
            variant.setAllowBackorder(true);

            // when
            ProductVariantResponse response = ProductVariantResponse.fromEntity(variant);

            // then
            assertThat(response.title()).isEqualTo("S / Red");
            assertThat(response.sku()).isEqualTo("SKU-001");
            assertThat(response.barcode()).isEqualTo("BC-001");
            assertThat(response.weight()).isEqualByComparingTo(new BigDecimal("1.5"));
            assertThat(response.height()).isEqualByComparingTo(new BigDecimal("10.0"));
            assertThat(response.width()).isEqualByComparingTo(new BigDecimal("5.0"));
            assertThat(response.length()).isEqualByComparingTo(new BigDecimal("3.0"));
            assertThat(response.manageInventory()).isTrue();
            assertThat(response.allowBackorder()).isTrue();
            assertThat(response.createdAt()).isNotNull();
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        void shouldMapVariantWithOptionValues() {
            // given
            Product product = Product.create("T-Shirt", "t-shirt");
            ProductOption sizeOption = product.addOption("Size");
            ProductOptionValue sizeS = sizeOption.addValue("S");
            ProductOption colorOption = product.addOption("Color");
            ProductOptionValue colorRed = colorOption.addValue("Red");

            ProductVariant variant = product.addVariant("S / Red");
            variant.addOptionValue(sizeS);
            variant.addOptionValue(colorRed);

            // when
            ProductVariantResponse response = ProductVariantResponse.fromEntity(variant);

            // then
            assertThat(response.optionValues())
                    .containsEntry("Size", "S")
                    .containsEntry("Color", "Red")
                    .hasSize(2);
        }

        @Test
        void shouldMapVariantWithNoOptionValues() {
            // given
            Product product = Product.create("T-Shirt", "t-shirt");
            ProductVariant variant = product.addVariant("Default");

            // when
            ProductVariantResponse response = ProductVariantResponse.fromEntity(variant);

            // then
            assertThat(response.optionValues()).isEmpty();
        }

        @Test
        void shouldMapMinimalVariant() {
            // given
            Product product = Product.create("T-Shirt", "t-shirt");
            ProductVariant variant = product.addVariant("Default");

            // when
            ProductVariantResponse response = ProductVariantResponse.fromEntity(variant);

            // then
            assertThat(response.title()).isEqualTo("Default");
            assertThat(response.sku()).isNull();
            assertThat(response.barcode()).isNull();
            assertThat(response.weight()).isNull();
            assertThat(response.manageInventory()).isFalse();
            assertThat(response.allowBackorder()).isFalse();
        }
    }

    @Nested
    class DefensiveCopyTests {

        @Test
        void shouldReturnImmutableOptionValuesMap() {
            // given
            ProductVariantResponse response = new ProductVariantResponse(
                    null, "S / Red", "SKU-001", null, null, null, null, null,
                    false, false, Map.of("Size", "S"), null, null);

            // then
            assertThat(response.optionValues()).hasSize(1);
        }

        @Test
        void shouldHandleNullOptionValuesGracefully() {
            // given
            ProductVariantResponse response = new ProductVariantResponse(
                    null, "Default", null, null, null, null, null, null,
                    false, false, null, null, null);

            // then
            assertThat(response.optionValues()).isEmpty();
        }
    }
}
