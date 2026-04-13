package com.inventory.demo.product.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity representing a configurable product option (e.g., "Size", "Color").
 * Each option belongs to a product and has one or more values.
 */
@Entity
@Table(name = "product_options")
@SQLRestriction("deleted_at IS NULL")
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Column(name = "title", nullable = false)
    private String title;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @SQLRestriction("deleted_at IS NULL")
    private final List<ProductOptionValue> values = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected ProductOption() {
        // JPA requires a no-arg constructor
    }

    private ProductOption(Product product, String title) {
        this.product = product;
        this.title = title;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Factory method to create a new product option.
     *
     * @param product the parent product
     * @param title   the option title (e.g., "Size", "Color")
     * @return a new ProductOption
     */
    public static ProductOption create(Product product, String title) {
        return new ProductOption(product, title);
    }

    /**
     * Adds a value to this option.
     *
     * @param value the value text (e.g., "S", "M", "L")
     * @return the created ProductOptionValue
     */
    public ProductOptionValue addValue(String value) {
        ProductOptionValue optionValue = ProductOptionValue.create(this, value);
        this.values.add(optionValue);
        this.updatedAt = Instant.now();
        return optionValue;
    }

    /**
     * Replaces all current values with the given list.
     * Existing values not in the new list are removed; new values are added.
     *
     * @param newValues the desired set of value strings
     */
    public void replaceValues(List<String> newValues) {
        this.values.clear();
        for (String val : newValues) {
            this.values.add(ProductOptionValue.create(this, val));
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Soft-deletes this option and all its values.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
        for (ProductOptionValue val : this.values) {
            val.softDelete();
        }
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

    public List<ProductOptionValue> getValues() {
        return Collections.unmodifiableList(values);
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
