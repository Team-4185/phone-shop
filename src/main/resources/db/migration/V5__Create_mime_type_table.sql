CREATE TABLE mime_types
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY,
    extension VARCHAR NOT NULL,
    type      VARCHAR NOT NULL,
    CONSTRAINT PK_mime_types_id PRIMARY KEY (id)
);