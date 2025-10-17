CREATE TABLE images
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY,
    name            VARCHAR   NOT NULL,
    minio_key       VARCHAR   NOT NULL,
    size            BIGINT    NOT NULL,
    fk_mime_type_id BIGINT    NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP,
    CONSTRAINT PK_images_id PRIMARY KEY (id),
    CONSTRAINT FK_images_fk_mime_type_id FOREIGN KEY (fk_mime_type_id)
        REFERENCES mime_types (id) ON DELETE RESTRICT
);