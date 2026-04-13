package com.inventory.demo.product.repository;

import com.inventory.demo.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for Product aggregate.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Checks whether a product with the given handle already exists.
     *
     * @param handle the URL-friendly product handle
     * @return true if a product with that handle exists
     */
    boolean existsByHandle(String handle);
}
