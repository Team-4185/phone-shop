CREATE TABLE phone_colors
(
    phone_id BIGINT      NOT NULL,
    color    VARCHAR(30) NOT NULL,

    CONSTRAINT fk_phone_colors_phone FOREIGN KEY (phone_id) REFERENCES phones (id) ON DELETE CASCADE,
    CONSTRAINT pk_phone_colors PRIMARY KEY (phone_id, color)
);

CREATE TABLE phone_storages
(
    phone_id BIGINT      NOT NULL,
    storage  VARCHAR(30) NOT NULL,

    CONSTRAINT fk_phone_storages_phone FOREIGN KEY (phone_id) REFERENCES phones (id) ON DELETE CASCADE,
    CONSTRAINT pk_phone_storages PRIMARY KEY (phone_id, storage)
);