CREATE TABLE orders
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP,
    fk_user_id BIGINT         NOT NULL,
    status     VARCHAR(20)    NOT NULL,
    total      DECIMAL(10, 2) NOT NULL,
    CONSTRAINT PK_orders_id PRIMARY KEY (id),
    CONSTRAINT FK_orders_fk_user_id FOREIGN KEY (fk_user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT CK_orders_status_valid CHECK (
        status IN ('NEW', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED')
    ),
    CONSTRAINT CK_orders_total_non_negative CHECK (total >= 0)
);

CREATE TABLE orders_items
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    created_at   TIMESTAMP      NOT NULL,
    updated_at   TIMESTAMP,
    fk_order_id  BIGINT         NOT NULL,
    fk_phone_id  BIGINT,
    product_name VARCHAR(255)   NOT NULL,
    sku          VARCHAR(64)    NOT NULL,
    unit_price   DECIMAL(10, 2) NOT NULL,
    quantity     INTEGER        NOT NULL,
    total_price  DECIMAL(10, 2) NOT NULL,
    CONSTRAINT PK_orders_items_id PRIMARY KEY (id),
    CONSTRAINT FK_orders_items_fk_order_id FOREIGN KEY (fk_order_id)
        REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT FK_orders_items_fk_phone_id FOREIGN KEY (fk_phone_id)
        REFERENCES phones (id) ON DELETE SET NULL,
    CONSTRAINT CK_orders_items_unit_price_non_negative CHECK (unit_price >= 0),
    CONSTRAINT CK_orders_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT CK_orders_items_total_price_non_negative CHECK (total_price >= 0)
);
