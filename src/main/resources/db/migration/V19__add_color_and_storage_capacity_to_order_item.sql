ALTER TABLE orders_items
    ADD COLUMN selected_color   VARCHAR(30) NOT NULL,
    ADD COLUMN selected_storage VARCHAR(30) NOT NULL;