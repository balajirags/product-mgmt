package com.inventory.demo.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a product in the catalog.
 * Products are always created in DRAFT status and support soft-delete via deleted_at.
 */
@Entity
@Table(name = "products")
@SQLRestriction("deleted_at IS NULL")
@SuppressWarnings("PMD.TooManyFields") // Domain entity mirrors wide database table
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "handle", unique = true)
    private String handle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "is_giftcard", nullable = false)
    private boolean giftcard;

    @Column(name = "discountable", nullable = false)
    private boolean discountable;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "height")
    private BigDecimal height;

    @Column(name = "width")
    private BigDecimal width;

    @Column(name = "\"length\"")
    private BigDecimal length;

    @Column(name = "hs_code")
    private String hsCode;

    @Column(name = "mid_code")
    private String midCode;

    @Column(name = "origin_country")
    private String originCountry;

    @Column(name = "material")
    private String material;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Product() {
        // JPA requires a no-arg constructor
    }

    private Product(String title, String handle) {
        this.title = title;
        this.handle = handle;
        this.status = ProductStatus.DRAFT;
        this.giftcard = false;
        this.discountable = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Factory method to create a new Product in DRAFT status.
     *
     * @param title  the product title (required)
     * @param handle the URL-friendly handle (may be null, must be unique when present)
     * @return a new Product in DRAFT status
     */
    public static Product create(String title, String handle) {
        return new Product(title, handle);
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getHandle() {
        return handle;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public boolean isGiftcard() {
        return giftcard;
    }

    public boolean isDiscountable() {
        return discountable;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public BigDecimal getLength() {
        return length;
    }

    public String getHsCode() {
        return hsCode;
    }

    public String getMidCode() {
        return midCode;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public String getMaterial() {
        return material;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getMetadata() {
        return metadata;
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
