CREATE TABLE favorites
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP,
    fk_user_id  BIGINT    NOT NULL,
    fk_phone_id BIGINT    NOT NULL,

    CONSTRAINT pk_favorites_id PRIMARY KEY (id),
    CONSTRAINT fk_favorites_fk_user_id FOREIGN KEY (fk_user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_fk_phone_id FOREIGN KEY (fk_phone_id)
        REFERENCES phones (id) ON DELETE CASCADE,
    CONSTRAINT uk_favorites_user_phone UNIQUE (fk_user_id, fk_phone_id)
);
