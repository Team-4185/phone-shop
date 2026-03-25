CREATE TABLE password_reset_tokens
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    user_id    BIGINT    NOT NULL,
    token_hash VARCHAR   NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at    TIMESTAMP,
    CONSTRAINT PK_password_reset_tokens_id PRIMARY KEY (id),
    CONSTRAINT FK_password_reset_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);