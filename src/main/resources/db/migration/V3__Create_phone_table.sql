CREATE TABLE phones
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP,
    name         VARCHAR   NOT NULL,
    description  TEXT,
    price        DECIMAL NOT NULL,
    brand        VARCHAR NOT NULL,
    release_year INT NOT NULL,
    CONSTRAINT PK_phones_id PRIMARY KEY (id)
);