package com.inventory.demo.product.repository;

import com.inventory.demo.product.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for ProductVariant persistence operations.
 */
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    /**
     * Checks whether a variant with the given SKU already exists.
     *
     * @param sku the SKU to check
     * @return true if a variant with this SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * Checks whether a variant with the given SKU exists, excluding a specific variant.
     *
     * @param sku       the SKU to check
     * @param excludeId the variant ID to exclude
     * @return true if another variant with this SKU exists
     */
    boolean existsBySkuAndIdNot(String sku, UUID excludeId);

    /**
     * Checks whether a variant with the given barcode already exists.
     *
     * @param barcode the barcode to check
     * @return true if a variant with this barcode exists
     */
    boolean existsByBarcode(String barcode);

    /**
     * Checks whether a variant with the given barcode exists, excluding a specific variant.
     *
     * @param barcode   the barcode to check
     * @param excludeId the variant ID to exclude
     * @return true if another variant with this barcode exists
     */
    boolean existsByBarcodeAndIdNot(String barcode, UUID excludeId);
}
