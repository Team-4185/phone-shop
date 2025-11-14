CREATE TABLE carts_items
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    fk_cart_id  BIGINT  NOT NULL,
    fk_phone_id BIGINT  NOT NULL,
    amount      INTEGER NOT NULL,
    CONSTRAINT PK_carts_items_id PRIMARY KEY (id),
    CONSTRAINT FK_carts_items_fk_cart_id
        FOREIGN KEY (fk_cart_id)
            REFERENCES carts (id)
            ON DELETE CASCADE,
    CONSTRAINT FK_carts_items_fk_phone_id
        FOREIGN KEY (fk_phone_id)
            REFERENCES phones (id)
            ON DELETE CASCADE,
    CONSTRAINT UQ_carts_items_cart_phone UNIQUE (fk_cart_id, fk_phone_id)
);
