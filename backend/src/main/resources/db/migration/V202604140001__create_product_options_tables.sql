CREATE TABLE product_options (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    product_id      UUID            NOT NULL,
    title           TEXT            NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ,

    CONSTRAINT pk_product_options PRIMARY KEY (id),
    CONSTRAINT fk_product_options__products__product_id
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uq_product_options__product_id_title
        UNIQUE (product_id, title)
);

CREATE INDEX idx_product_options__product_id ON product_options(product_id);

CREATE TABLE product_option_values (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    option_id       UUID            NOT NULL,
    value           TEXT            NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ,

    CONSTRAINT pk_product_option_values PRIMARY KEY (id),
    CONSTRAINT fk_product_option_values__product_options__option_id
        FOREIGN KEY (option_id) REFERENCES product_options(id) ON DELETE CASCADE,
    CONSTRAINT uq_product_option_values__option_id_value
        UNIQUE (option_id, value)
);

CREATE INDEX idx_product_option_values__option_id ON product_option_values(option_id);
