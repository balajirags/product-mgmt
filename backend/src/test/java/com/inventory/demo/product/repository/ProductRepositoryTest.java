package com.inventory.demo.product.repository;

import com.inventory.demo.product.domain.Product;
import com.inventory.demo.product.domain.ProductStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    class PersistenceTests {

        @Test
        void shouldPersistAndRetrieveProduct() {
            // given
            Product product = Product.create("Running Shoes", "running-shoes");

            // when
            Product saved = productRepository.saveAndFlush(product);
            entityManager.clear();
            Optional<Product> found = productRepository.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getTitle()).isEqualTo("Running Shoes");
            assertThat(found.get().getHandle()).isEqualTo("running-shoes");
            assertThat(found.get().getStatus()).isEqualTo(ProductStatus.DRAFT);
            assertThat(found.get().isGiftcard()).isFalse();
            assertThat(found.get().isDiscountable()).isTrue();
            assertThat(found.get().getCreatedAt()).isNotNull();
            assertThat(found.get().getUpdatedAt()).isNotNull();
        }

        @Test
        void shouldAutoGenerateUuid() {
            // given
            Product product = Product.create("T-Shirt", "t-shirt");

            // when
            Product saved = productRepository.saveAndFlush(product);

            // then
            assertThat(saved.getId()).isNotNull();
        }

        @Test
        void shouldPersistProductWithNullHandle() {
            // given
            Product product = Product.create("Gift Card", null);

            // when
            Product saved = productRepository.saveAndFlush(product);
            entityManager.clear();
            Optional<Product> found = productRepository.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getHandle()).isNull();
        }
    }

    @Nested
    class HandleUniquenessTests {

        @Test
        void shouldEnforceHandleUniqueness() {
            // given
            Product first = Product.create("Hoodie v1", "hoodie");
            productRepository.saveAndFlush(first);
            entityManager.clear();

            Product duplicate = Product.create("Hoodie v2", "hoodie");

            // when / then
            assertThatThrownBy(() -> {
                productRepository.saveAndFlush(duplicate);
                entityManager.flush();
            }).isInstanceOf(Exception.class);
        }

        @Test
        void shouldAllowMultipleNullHandles() {
            // given
            Product first = Product.create("Card A", null);
            Product second = Product.create("Card B", null);

            // when
            productRepository.saveAndFlush(first);
            productRepository.saveAndFlush(second);
            entityManager.clear();

            // then
            assertThat(productRepository.count()).isGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    class QueryMethodTests {

        @Test
        void shouldReturnTrueWhenHandleExists() {
            // given
            Product product = Product.create("Cap", "cap");
            productRepository.saveAndFlush(product);
            entityManager.clear();

            // when / then
            assertThat(productRepository.existsByHandle("cap")).isTrue();
        }

        @Test
        void shouldReturnFalseWhenHandleDoesNotExist() {
            // when / then
            assertThat(productRepository.existsByHandle("nonexistent")).isFalse();
        }

        @Test
        void shouldSupportExistsById() {
            // given
            Product product = Product.create("Socks", "socks");
            Product saved = productRepository.saveAndFlush(product);

            // when / then
            assertThat(productRepository.existsById(saved.getId())).isTrue();
        }
    }
}
