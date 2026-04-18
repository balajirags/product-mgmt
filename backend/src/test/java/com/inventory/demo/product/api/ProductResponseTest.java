package com.inventory.demo.product.api;

import com.inventory.demo.product.domain.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductResponseTest {

    @Test
    void shouldMapAllFieldsFromEntity() {
        // given
        Product product = Product.create("Test Product", "test-product");
        product.describeAs("A description");
        product.assignSubtitle("A subtitle");
        product.applyDimensions(
                new BigDecimal("1.5"),
                new BigDecimal("10.0"),
                new BigDecimal("5.0"),
                new BigDecimal("3.0")
        );
        product.attachMetadata(Map.of("key", "value"));
        product.assignExternalId("EXT-001");

        // when
        ProductResponse response = ProductResponse.fromEntity(product);

        // then
        assertThat(response.title()).isEqualTo("Test Product");
        assertThat(response.handle()).isEqualTo("test-product");
        assertThat(response.status()).isEqualTo("DRAFT");
        assertThat(response.description()).isEqualTo("A description");
        assertThat(response.subtitle()).isEqualTo("A subtitle");
        assertThat(response.giftcard()).isFalse();
        assertThat(response.discountable()).isTrue();
        assertThat(response.weight()).isEqualByComparingTo(new BigDecimal("1.5"));
        assertThat(response.height()).isEqualByComparingTo(new BigDecimal("10.0"));
        assertThat(response.width()).isEqualByComparingTo(new BigDecimal("5.0"));
        assertThat(response.length()).isEqualByComparingTo(new BigDecimal("3.0"));
        assertThat(response.metadata()).containsEntry("key", "value");
        assertThat(response.externalId()).isEqualTo("EXT-001");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void shouldMapMinimalEntityWithNullOptionalFields() {
        // given
        Product product = Product.create("Minimal Product", "minimal");

        // when
        ProductResponse response = ProductResponse.fromEntity(product);

        // then
        assertThat(response.title()).isEqualTo("Minimal Product");
        assertThat(response.handle()).isEqualTo("minimal");
        assertThat(response.description()).isNull();
        assertThat(response.subtitle()).isNull();
        assertThat(response.weight()).isNull();
        assertThat(response.metadata()).isNull();
        assertThat(response.externalId()).isNull();
    }
}
