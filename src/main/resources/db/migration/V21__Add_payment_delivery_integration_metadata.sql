ALTER TABLE orders
    ADD COLUMN payment_provider VARCHAR(40) NOT NULL DEFAULT 'mock',
    ADD COLUMN delivery_provider VARCHAR(40) NOT NULL DEFAULT 'mock',
    ADD COLUMN pickup_point_id VARCHAR(120),
    ADD COLUMN estimated_delivery_date DATE,
    ADD COLUMN delivery_price DECIMAL(10, 2) NOT NULL DEFAULT 0;

ALTER TABLE orders
    ALTER COLUMN payment_provider DROP DEFAULT,
    ALTER COLUMN delivery_provider DROP DEFAULT,
    ALTER COLUMN delivery_price DROP DEFAULT;

CREATE TABLE processed_payment_events
(
    id                      BIGINT GENERATED ALWAYS AS IDENTITY,
    created_at              TIMESTAMP    NOT NULL,
    updated_at              TIMESTAMP,
    provider                VARCHAR(40)  NOT NULL,
    external_event_id       VARCHAR(120) NOT NULL,
    external_transaction_id VARCHAR(120) NOT NULL,
    CONSTRAINT PK_processed_payment_events_id PRIMARY KEY (id),
    CONSTRAINT UQ_processed_payment_events_external_event_id UNIQUE (external_event_id)
);
