CREATE TABLE carts
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP,
    total_price  DECIMAL(10, 2) NOT NULL ,

    fk_user_id  BIGINT    NOT NULL UNIQUE,

    CONSTRAINT PK_carts_id PRIMARY KEY (id),
    CONSTRAINT FK_carts_fk_user_id FOREIGN KEY (fk_user_id)
        REFERENCES users (id) ON DELETE CASCADE
);
