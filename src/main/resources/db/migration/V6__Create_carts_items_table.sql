CREATE TABLE carts_items
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    fk_cart_id  BIGINT  NOT NULL,
    fk_phone_id BIGINT  NOT NULL,
    amount      INTEGER NOT NULL,

    CONSTRAINT PK_carts_items_id PRIMARY KEY (id),

    CONSTRAINT fk_carts_items_cart
        FOREIGN KEY (fk_cart_id)
            REFERENCES carts (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_carts_items_phone
        FOREIGN KEY (fk_phone_id)
            REFERENCES phones (id)
            ON DELETE CASCADE
);
