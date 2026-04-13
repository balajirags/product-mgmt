package com.inventory.demo.product.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ProductTest {

    @Nested
    class FactoryMethodTests {

        @Test
        void shouldCreateProductInDraftStatus() {
            // when
            Product product = Product.create("Running Shoes", "running-shoes");

            // then
            assertThat(product.getTitle()).isEqualTo("Running Shoes");
            assertThat(product.getHandle()).isEqualTo("running-shoes");
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        }

        @Test
        void shouldCreateProductWithNullHandle() {
            // when
            Product product = Product.create("Gift Card", null);

            // then
            assertThat(product.getTitle()).isEqualTo("Gift Card");
            assertThat(product.getHandle()).isNull();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        }

        @Test
        void shouldSetDefaultBooleanValues() {
            // when
            Product product = Product.create("T-Shirt", "t-shirt");

            // then
            assertThat(product.isGiftcard()).isFalse();
            assertThat(product.isDiscountable()).isTrue();
        }

        @Test
        void shouldSetTimestampsOnCreation() {
            // when
            Product product = Product.create("Hoodie", "hoodie");

            // then
            assertThat(product.getCreatedAt()).isNotNull();
            assertThat(product.getUpdatedAt()).isNotNull();
            assertThat(product.getCreatedAt()).isCloseTo(
                    product.getUpdatedAt(),
                    within(1, java.time.temporal.ChronoUnit.SECONDS));
        }

        @Test
        void shouldNotHaveIdBeforePersistence() {
            // when
            Product product = Product.create("Jacket", "jacket");

            // then
            assertThat(product.getId()).isNull();
        }
    }

    @Nested
    class DefaultValueTests {

        @Test
        void shouldHaveNullOptionalFieldsByDefault() {
            // when
            Product product = Product.create("Cap", "cap");

            // then
            assertThat(product.getDescription()).isNull();
            assertThat(product.getSubtitle()).isNull();
            assertThat(product.getThumbnail()).isNull();
            assertThat(product.getWeight()).isNull();
            assertThat(product.getHeight()).isNull();
            assertThat(product.getWidth()).isNull();
            assertThat(product.getLength()).isNull();
            assertThat(product.getHsCode()).isNull();
            assertThat(product.getMidCode()).isNull();
            assertThat(product.getOriginCountry()).isNull();
            assertThat(product.getMaterial()).isNull();
            assertThat(product.getExternalId()).isNull();
            assertThat(product.getMetadata()).isNull();
        }

        @Test
        void shouldHaveNullDeletedAtByDefault() {
            // when
            Product product = Product.create("Socks", "socks");

            // then
            assertThat(product.getDeletedAt()).isNull();
        }
    }
}
