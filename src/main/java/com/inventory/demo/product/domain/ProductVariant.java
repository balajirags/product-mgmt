package com.inventory.demo.product.domain;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA entity representing a purchasable product variant (a unique combination of option values).
 * Each variant belongs to a product and may be linked to specific option values.
 */
@Entity
@Table(name = "product_variants")
@SQLRestriction("deleted_at IS NULL")
@SuppressWarnings("PMD.TooManyFields")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "sku")
    private String sku;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "height")
    private BigDecimal height;

    @Column(name = "width")
    private BigDecimal width;

    @Column(name = "\"length\"")
    private BigDecimal length;

    @Column(name = "manage_inventory", nullable = false)
    private boolean manageInventory;

    @Column(name = "allow_backorder", nullable = false)
    private boolean allowBackorder;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "variant_option_values",
            joinColumns = @JoinColumn(name = "variant_id"),
            inverseJoinColumns = @JoinColumn(name = "option_value_id")
    )
    private final List<ProductOptionValue> optionValues = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected ProductVariant() {
        // JPA requires a no-arg constructor
    }

    private ProductVariant(Product product, String title) {
        this.product = product;
        this.title = title;
        this.manageInventory = false;
        this.allowBackorder = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Factory method to create a new product variant.
     *
     * @param product the parent product
     * @param title   the variant title (e.g., "S / Red")
     * @return a new ProductVariant
     */
    public static ProductVariant create(Product product, String title) {
        return new ProductVariant(product, title);
    }

    /**
     * Assigns the SKU identifier for this variant.
     *
     * @param sku the stock-keeping unit code
     */
    public void assignSku(String sku) {
        this.sku = sku;
        this.updatedAt = Instant.now();
    }

    /**
     * Assigns the barcode for this variant.
     *
     * @param barcode the barcode string
     */
    public void assignBarcode(String barcode) {
        this.barcode = barcode;
        this.updatedAt = Instant.now();
    }

    /**
     * Sets the physical dimensions and weight.
     *
     * @param weight the variant weight
     * @param height the variant height
     * @param width  the variant width
     * @param length the variant length
     */
    public void applyDimensions(BigDecimal weight, BigDecimal height,
                                BigDecimal width, BigDecimal length) {
        this.weight = weight;
        this.height = height;
        this.width = width;
        this.length = length;
        this.updatedAt = Instant.now();
    }

    /**
     * Sets the inventory management flag.
     *
     * @param manageInventory true if inventory should be tracked for this variant
     */
    public void setManageInventory(boolean manageInventory) {
        this.manageInventory = manageInventory;
        this.updatedAt = Instant.now();
    }

    /**
     * Sets the allow-backorder flag.
     *
     * @param allowBackorder true if backorders are allowed for this variant
     */
    public void setAllowBackorder(boolean allowBackorder) {
        this.allowBackorder = allowBackorder;
        this.updatedAt = Instant.now();
    }

    /**
     * Links a product option value to this variant.
     *
     * @param optionValue the option value to associate
     */
    public void addOptionValue(ProductOptionValue optionValue) {
        this.optionValues.add(optionValue);
        this.updatedAt = Instant.now();
    }

    /**
     * Replaces all option value associations.
     *
     * @param newOptionValues the new list of option values
     */
    public void replaceOptionValues(List<ProductOptionValue> newOptionValues) {
        this.optionValues.clear();
        this.optionValues.addAll(newOptionValues);
        this.updatedAt = Instant.now();
    }

    /**
     * Soft-deletes this variant.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Generates a title from the assigned option values (e.g., "S / Red").
     *
     * @return the auto-generated title string
     */
    public String generateTitle() {
        return optionValues.stream()
                .map(ProductOptionValue::getValue)
                .collect(Collectors.joining(" / "));
    }

    /**
     * Updates the variant title.
     *
     * @param title the new title
     */
    public void updateTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "JPA-managed entity — getProduct() must return the owning entity for framework use")
    public Product getProduct() {
        return product;
    }

    public String getTitle() {
        return title;
    }

    public String getSku() {
        return sku;
    }

    public String getBarcode() {
        return barcode;
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

    public boolean isManageInventory() {
        return manageInventory;
    }

    public boolean isAllowBackorder() {
        return allowBackorder;
    }

    /**
     * Returns an unmodifiable view of the variant's option values.
     *
     * @return the option values
     */
    public List<ProductOptionValue> getOptionValues() {
        return Collections.unmodifiableList(optionValues);
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
