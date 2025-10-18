CREATE TABLE mime_types
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY,
    extension VARCHAR(32)  NOT NULL UNIQUE,
    type      VARCHAR(128) NOT NULL,
    CONSTRAINT PK_mime_types_id PRIMARY KEY (id)
);