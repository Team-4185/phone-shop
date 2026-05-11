CREATE TABLE orders
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP,
    fk_user_id           BIGINT,
    customer_email       VARCHAR(100)   NOT NULL,
    customer_first_name  VARCHAR(100),
    customer_last_name   VARCHAR(100),
    customer_phone_number VARCHAR(20),
    customer_city        VARCHAR(100),
    status               VARCHAR(20)    NOT NULL,
    payment_method       VARCHAR(30)    NOT NULL,
    delivery_method      VARCHAR(30)    NOT NULL,
    total                DECIMAL(10, 2) NOT NULL,
    CONSTRAINT PK_orders_id PRIMARY KEY (id),
    CONSTRAINT FK_orders_fk_user_id FOREIGN KEY (fk_user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT CK_orders_status_valid CHECK (
        status IN ('NEW', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED')
    ),
    CONSTRAINT CK_orders_payment_method_valid CHECK (
        payment_method IN ('CARD', 'CASH_ON_DELIVERY')
    ),
    CONSTRAINT CK_orders_delivery_method_valid CHECK (
        delivery_method IN ('COURIER', 'POST_OFFICE', 'PICKUP')
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
