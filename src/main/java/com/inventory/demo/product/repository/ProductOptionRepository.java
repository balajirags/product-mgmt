package com.inventory.demo.product.repository;

import com.inventory.demo.product.domain.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for ProductOption entities.
 */
@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, UUID> {

    /**
     * Checks whether an option with the given title already exists for a product.
     *
     * @param productId the product UUID
     * @param title     the option title
     * @return true if a matching option exists
     */
    boolean existsByProductIdAndTitle(UUID productId, String title);
}
