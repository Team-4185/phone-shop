CREATE TABLE users
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP,
    email        VARCHAR   NOT NULL UNIQUE,
    password     VARCHAR   NOT NULL,
    first_name   VARCHAR,
    last_name    VARCHAR,
    city         VARCHAR,
    phone_number VARCHAR,
    fk_role_id   BIGINT    NOT NULL,
    CONSTRAINT PK_users_id PRIMARY KEY (id),
    CONSTRAINT FK_users_fk_role_id FOREIGN KEY (fk_role_id)
        REFERENCES roles (id) ON DELETE RESTRICT
);