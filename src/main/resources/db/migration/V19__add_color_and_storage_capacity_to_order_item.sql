ALTER TABLE orders_items
    ADD COLUMN selected_color   VARCHAR(30),
    ADD COLUMN selected_storage VARCHAR(30);

UPDATE orders_items oi
SET selected_color = COALESCE(
        (
            SELECT pc.color
            FROM phone_colors pc
            WHERE pc.phone_id = oi.fk_phone_id
            LIMIT 1
        ),
        'BLACK'
    ),
    selected_storage = COALESCE(
        (
            SELECT ps.storage
            FROM phone_storages ps
            WHERE ps.phone_id = oi.fk_phone_id
            LIMIT 1
        ),
        'CAPACITY_128GB'
    )
WHERE selected_color IS NULL
   OR selected_storage IS NULL;

ALTER TABLE orders_items
    ALTER COLUMN selected_color SET NOT NULL,
    ALTER COLUMN selected_storage SET NOT NULL;
