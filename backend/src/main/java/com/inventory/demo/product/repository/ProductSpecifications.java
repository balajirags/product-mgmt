package com.inventory.demo.product.repository;

import com.inventory.demo.product.domain.Product;
import com.inventory.demo.product.domain.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for dynamic Product queries.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
        // Utility class
    }

    /**
     * Filters products by status.
     *
     * @param status the product status to filter by
     * @return a specification matching products with the given status
     */
    public static Specification<Product> withStatus(ProductStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
