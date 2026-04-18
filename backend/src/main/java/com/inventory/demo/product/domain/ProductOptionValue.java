package com.inventory.demo.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a single value for a product option.
 * For example, if the option is "Size", values might be "S", "M", "L".
 */
@Entity
@Table(name = "product_option_values")
@SQLRestriction("deleted_at IS NULL")
public class ProductOptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false, updatable = false)
    private ProductOption option;

    @Column(name = "\"value\"", nullable = false)
    private String value;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected ProductOptionValue() {
        // JPA requires a no-arg constructor
    }

    private ProductOptionValue(ProductOption option, String value) {
        this.option = option;
        this.value = value;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Factory method to create a new option value.
     *
     * @param option the parent option
     * @param value  the option value text (e.g., "S", "Red")
     * @return a new ProductOptionValue
     */
    public static ProductOptionValue create(ProductOption option, String value) {
        return new ProductOptionValue(option, value);
    }

    /**
     * Soft-deletes this option value.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "JPA-managed entity — getOption() must return the owning entity for framework use")
    public ProductOption getOption() {
        return option;
    }

    public String getValue() {
        return value;
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
