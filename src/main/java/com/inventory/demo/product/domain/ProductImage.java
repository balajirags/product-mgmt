package com.inventory.demo.product.domain;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a product gallery image.
 * Each image belongs to a product and has a rank (position) within the gallery.
 */
@Entity
@Table(name = "product_images")
@SQLRestriction("deleted_at IS NULL")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "JPA-managed bidirectional association")
    private Product product;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "rank", nullable = false)
    private int rank;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected ProductImage() {
        // JPA requires a no-arg constructor
    }

    private ProductImage(Product product, String url, int rank) {
        this.product = product;
        this.url = url;
        this.rank = rank;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Factory method to create a new product image.
     *
     * @param product the parent product
     * @param url     the image URL (must not be blank)
     * @param rank    the display position (0-based)
     * @return a new ProductImage
     */
    public static ProductImage create(Product product, String url, int rank) {
        return new ProductImage(product, url, rank);
    }

    /**
     * Updates the display rank of this image.
     *
     * @param rank the new rank
     */
    public void updateRank(int rank) {
        this.rank = rank;
        this.updatedAt = Instant.now();
    }

    /**
     * Soft-deletes this image by setting the deleted_at timestamp.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "JPA-managed bidirectional association")
    public Product getProduct() {
        return product;
    }

    public String getUrl() {
        return url;
    }

    public int getRank() {
        return rank;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
