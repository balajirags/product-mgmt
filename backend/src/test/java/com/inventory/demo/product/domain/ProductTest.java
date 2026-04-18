package com.inventory.demo.product.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Nested
    class UpdateTitleTests {

        @Test
        void shouldUpdateTitle() {
            // given
            Product product = Product.create("Original", "original");

            // when
            product.updateTitle("Updated");

            // then
            assertThat(product.getTitle()).isEqualTo("Updated");
        }

        @Test
        void shouldRefreshUpdatedAtOnTitleChange() {
            // given
            Product product = Product.create("Original", "original");
            Instant before = product.getUpdatedAt();

            // when
            product.updateTitle("Updated");

            // then
            assertThat(product.getUpdatedAt()).isAfterOrEqualTo(before);
        }
    }

    @Nested
    class UpdateHandleTests {

        @Test
        void shouldUpdateHandle() {
            // given
            Product product = Product.create("Product", "old-handle");

            // when
            product.updateHandle("new-handle");

            // then
            assertThat(product.getHandle()).isEqualTo("new-handle");
        }

        @Test
        void shouldRefreshUpdatedAtOnHandleChange() {
            // given
            Product product = Product.create("Product", "old-handle");
            Instant before = product.getUpdatedAt();

            // when
            product.updateHandle("new-handle");

            // then
            assertThat(product.getUpdatedAt()).isAfterOrEqualTo(before);
        }
    }

    @Nested
    class MarkUpdatedTests {

        @Test
        void shouldRefreshUpdatedAtTimestamp() {
            // given
            Product product = Product.create("Product", "product");
            Instant before = product.getUpdatedAt();

            // when
            product.markUpdated();

            // then
            assertThat(product.getUpdatedAt()).isAfterOrEqualTo(before);
        }
    }

    @Nested
    class OptionManagementTests {

        @Test
        void shouldAddOptionToProduct() {
            // given
            Product product = Product.create("T-Shirt", "t-shirt");

            // when
            ProductOption option = product.addOption("Size");

            // then
            assertThat(option.getTitle()).isEqualTo("Size");
            assertThat(option.getProduct()).isSameAs(product);
            assertThat(product.getOptions()).hasSize(1);
        }

        @Test
        void shouldAddMultipleOptions() {
            // given
            Product product = Product.create("T-Shirt", "t-shirt");

            // when
            product.addOption("Size");
            product.addOption("Color");
            product.addOption("Material");

            // then
            assertThat(product.getOptions()).hasSize(3);
        }

        @Test
        void shouldReturnEmptyOptionsListByDefault() {
            // when
            Product product = Product.create("Simple", "simple");

            // then
            assertThat(product.getOptions()).isEmpty();
        }

        @Test
        void shouldReturnUnmodifiableOptionsList() {
            // given
            Product product = Product.create("T-Shirt", "t-shirt");
            product.addOption("Size");

            // when / then
            assertThatThrownBy(() -> product.getOptions().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
