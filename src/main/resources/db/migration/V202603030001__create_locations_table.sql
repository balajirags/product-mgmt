CREATE TABLE locations (
    id              UUID            NOT NULL DEFAULT gen_random_uuid(),
    location_code   TEXT            NOT NULL,
    location_name   TEXT            NOT NULL,
    location_type   TEXT            NOT NULL,
    lifecycle_state TEXT            NOT NULL DEFAULT 'DRAFT',
    street          TEXT            NOT NULL,
    city            TEXT            NOT NULL,
    state           TEXT            NOT NULL,
    postal_code     TEXT            NOT NULL,
    country         TEXT            NOT NULL,
    contact_person  TEXT            NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT pk_locations PRIMARY KEY (id),
    CONSTRAINT uq_locations__code_name UNIQUE (location_code, location_name),
    CONSTRAINT ck_locations__location_type CHECK (location_type IN ('WAREHOUSE', 'STORE', 'DARKSTORE', 'THREE_PL_NODE')),
    CONSTRAINT ck_locations__lifecycle_state CHECK (lifecycle_state IN ('DRAFT', 'ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_locations__location_code CHECK (location_code IN ('WH-MUM', 'WH-CHN', 'WH-BEN'))
);
