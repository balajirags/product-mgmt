CREATE TABLE product_variants (
    id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
    product_id          UUID            NOT NULL,
    title               TEXT            NOT NULL,
    sku                 TEXT,
    barcode             TEXT,
    weight              NUMERIC(12,4),
    height              NUMERIC(12,4),
    width               NUMERIC(12,4),
    "length"            NUMERIC(12,4),
    manage_inventory    BOOLEAN         NOT NULL DEFAULT FALSE,
    allow_backorder     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    deleted_at          TIMESTAMPTZ,

    CONSTRAINT pk_product_variants PRIMARY KEY (id),
    CONSTRAINT fk_product_variants__products__product_id
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uq_product_variants__sku
        UNIQUE (sku),
    CONSTRAINT uq_product_variants__barcode
        UNIQUE (barcode)
);

CREATE INDEX idx_product_variants__product_id ON product_variants(product_id);

CREATE TABLE variant_option_values (
    variant_id          UUID            NOT NULL,
    option_value_id     UUID            NOT NULL,

    CONSTRAINT pk_variant_option_values PRIMARY KEY (variant_id, option_value_id),
    CONSTRAINT fk_variant_option_values__product_variants__variant_id
        FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    CONSTRAINT fk_variant_option_values__product_option_values__option_value_id
        FOREIGN KEY (option_value_id) REFERENCES product_option_values(id) ON DELETE CASCADE
);
