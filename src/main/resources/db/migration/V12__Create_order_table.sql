CREATE TABLE orders
(
    id                   BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL,
    fk_user_id           BIGINT,
    created_at           TIMESTAMP                           NOT NULL,
    updated_at           TIMESTAMP,
    total_price          DECIMAL(10, 2)                      NOT NULL,
    status               VARCHAR(100)                        NOT NULL,
    address              VARCHAR(500)                        NOT NULL,
    recipient_email      VARCHAR(100)                        NOT NULL,
    recipient_phone      VARCHAR(13)                         NOT NULL,
    recipient_firstname  VARCHAR(255)                        NOT NULL,
    recipient_lastname   VARCHAR(255)                        NOT NULL,
    payment_method       VARCHAR(100)                        NOT NULL,
    payment_intent_id    VARCHAR(255),
    payment_checkout_url VARCHAR(1000),
    payment_paid         BOOLEAN                             NOT NULL,
    processed_by_webhook BOOLEAN                             NOT NULL,
    CONSTRAINT PK_orders_id PRIMARY KEY (id),
    CONSTRAINT FK_orders_fk_user_id FOREIGN KEY (fk_user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT CHK_total_price_is_positive CHECK (total_price > 0.0)
);
-- fk_user_id is nullable, cause if user will be deleted, his all orders will be in DB


-- OrderItem
CREATE TABLE orders_phones
(
    fk_order_id BIGINT  NOT NULL,
    fk_phone_id BIGINT  NOT NULL,
    amount      INTEGER NOT NULL,
    CONSTRAINT PK_fk_order_id_and_fk_phone_id PRIMARY KEY (fk_order_id, fk_phone_id),
    CONSTRAINT FK_orders_phones_order_id FOREIGN KEY (fk_order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT FK_orders_phones_phone_id FOREIGN KEY (fk_phone_id) REFERENCES phones (id) ON DELETE RESTRICT,
    CONSTRAINT CHK_amount_is_positive CHECK (amount > 0)
);