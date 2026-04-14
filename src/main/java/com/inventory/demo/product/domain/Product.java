package com.inventory.demo.product.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @SQLRestriction("deleted_at IS NULL")
    private final List<ProductOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @SQLRestriction("deleted_at IS NULL")
    private final List<ProductVariant> variants = new ArrayList<>();

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

    /**
     * Applies an explicit product status.
     *
     * @param status the desired product status
     */
    public void applyStatus(ProductStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    /**
     * Sets the product description text.
     *
     * @param description the product description
     */
    public void describeAs(String description) {
        this.description = description;
    }

    /**
     * Sets the product subtitle.
     *
     * @param subtitle the product subtitle
     */
    public void assignSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Sets the physical dimensions and weight.
     *
     * @param weight the product weight
     * @param height the product height
     * @param width  the product width
     * @param length the product length
     */
    public void applyDimensions(BigDecimal weight, BigDecimal height,
                                BigDecimal width, BigDecimal length) {
        this.weight = weight;
        this.height = height;
        this.width = width;
        this.length = length;
    }

    /**
     * Sets the serialised JSON metadata.
     *
     * @param metadata the JSON metadata string
     */
    public void attachMetadata(String metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the external system identifier.
     *
     * @param externalId the external ID
     */
    public void assignExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * Updates the product title.
     *
     * @param title the new product title (must not be blank)
     */
    public void updateTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the URL-friendly handle.
     *
     * @param handle the new product handle
     */
    public void updateHandle(String handle) {
        this.handle = handle;
        this.updatedAt = Instant.now();
    }

    /**
     * Refreshes the updatedAt timestamp to the current instant.
     */
    public void markUpdated() {
        this.updatedAt = Instant.now();
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

    /**
     * Adds a new option to this product.
     *
     * @param title the option title (e.g., "Size", "Color")
     * @return the created ProductOption
     */
    public ProductOption addOption(String title) {
        ProductOption option = ProductOption.create(this, title);
        this.options.add(option);
        return option;
    }

    /**
     * Returns an unmodifiable view of the product options.
     *
     * @return the product options
     */
    public List<ProductOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    /**
     * Returns the mutable options list for internal modification.
     *
     * @return the mutable options list
     */
    List<ProductOption> getOptionsMutable() {
        return options;
    }

    /**
     * Adds a new variant to this product.
     *
     * @param title the variant title (e.g., "S / Red")
     * @return the created ProductVariant
     */
    public ProductVariant addVariant(String title) {
        ProductVariant variant = ProductVariant.create(this, title);
        this.variants.add(variant);
        return variant;
    }

    /**
     * Returns an unmodifiable view of the product variants.
     *
     * @return the product variants
     */
    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    /**
     * Returns the mutable variants list for internal modification.
     *
     * @return the mutable variants list
     */
    List<ProductVariant> getVariantsMutable() {
        return variants;
    }

    /**
     * Soft-deletes this product by setting the deleted_at timestamp.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
