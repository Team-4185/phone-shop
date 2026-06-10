CREATE TABLE product_variants
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    fk_phone_id      BIGINT         NOT NULL,
    sku              VARCHAR(64)    NOT NULL,
    color            VARCHAR(30)    NOT NULL,
    storage_capacity VARCHAR(30)    NOT NULL,
    price            DECIMAL(10, 2) NOT NULL,
    stock            INTEGER        NOT NULL,
    status           VARCHAR(30)    NOT NULL,
    CONSTRAINT pk_product_variants_id PRIMARY KEY (id),
    CONSTRAINT fk_product_variants_phone
        FOREIGN KEY (fk_phone_id)
            REFERENCES phones (id)
            ON DELETE CASCADE,
    CONSTRAINT uk_product_variants_sku UNIQUE (sku),
    CONSTRAINT uk_product_variants_phone_color_storage UNIQUE (fk_phone_id, color, storage_capacity),
    CONSTRAINT ck_product_variants_price_non_negative CHECK (price >= 0),
    CONSTRAINT ck_product_variants_stock_non_negative CHECK (stock >= 0),
    CONSTRAINT ck_product_variants_status_valid CHECK (
        status IN ('IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK')
    )
);

INSERT INTO product_variants (fk_phone_id, sku, color, storage_capacity, price, stock, status)
SELECT p.id,
       LEFT(
           p.sku || '-' ||
           REPLACE(COALESCE(pc.color, 'BLACK'), 'CAPACITY_', '') || '-' ||
           REPLACE(COALESCE(ps.storage, 'CAPACITY_128GB'), 'CAPACITY_', ''),
           64
       ) AS variant_sku,
       COALESCE(pc.color, 'BLACK') AS color,
       COALESCE(ps.storage, 'CAPACITY_128GB') AS storage_capacity,
       p.price,
       p.stock,
       p.status
FROM phones p
LEFT JOIN phone_colors pc ON pc.phone_id = p.id
LEFT JOIN phone_storages ps ON ps.phone_id = p.id
ON CONFLICT (fk_phone_id, color, storage_capacity) DO NOTHING;

ALTER TABLE carts_items
    ADD COLUMN fk_variant_id BIGINT;

UPDATE carts_items ci
SET fk_variant_id = (
    SELECT pv.id
    FROM product_variants pv
    WHERE pv.fk_phone_id = ci.fk_phone_id
    ORDER BY pv.price ASC, pv.id ASC
    LIMIT 1
)
WHERE fk_variant_id IS NULL;

ALTER TABLE carts_items
    ALTER COLUMN fk_variant_id SET NOT NULL,
    ADD CONSTRAINT fk_carts_items_fk_variant_id
        FOREIGN KEY (fk_variant_id)
            REFERENCES product_variants (id)
            ON DELETE CASCADE;

ALTER TABLE carts_items
    DROP CONSTRAINT IF EXISTS uq_carts_items_cart_phone,
    ADD CONSTRAINT uq_carts_items_cart_variant UNIQUE (fk_cart_id, fk_variant_id);

ALTER TABLE orders_items
    ADD COLUMN fk_variant_id BIGINT;

UPDATE orders_items oi
SET fk_variant_id = (
    SELECT pv.id
    FROM product_variants pv
    WHERE pv.fk_phone_id = oi.fk_phone_id
      AND pv.color = oi.selected_color
      AND pv.storage_capacity = oi.selected_storage
    ORDER BY pv.id ASC
    LIMIT 1
)
WHERE fk_variant_id IS NULL;

ALTER TABLE orders_items
    ADD CONSTRAINT fk_orders_items_fk_variant_id
        FOREIGN KEY (fk_variant_id)
            REFERENCES product_variants (id)
            ON DELETE SET NULL;
