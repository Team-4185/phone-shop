ALTER TABLE images
    ADD COLUMN fk_phone_id BIGINT;

ALTER TABLE images
    ADD CONSTRAINT FK_images_fk_phone_id FOREIGN KEY (fk_phone_id)
        REFERENCES phones (id) ON DELETE CASCADE;