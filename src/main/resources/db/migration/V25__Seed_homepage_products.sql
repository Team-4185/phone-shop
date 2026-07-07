INSERT INTO phones (id, name, description, price, brand, release_year, sku, stock, status,
                    cpu, cores_number, screen_size, front_camera, main_camera, battery_capacity,
                    created_at, updated_at)
    OVERRIDING SYSTEM VALUE
VALUES (1, 'iPhone 17 Pro',
        'Apple flagship phone for the homepage hero section.', 1000.00, 'Apple', 2026,
        'APL-IP17P-001', 50, 'IN_STOCK',
        'Apple A19 Pro', 6, '6.3"', '24 MP', '48-48-48 MP', '4200 mAh',
        NOW() - INTERVAL '3 days', NULL),
       (2, 'Xiaomi 15 Pro',
        'Premium Xiaomi phone with Leica camera and fast charging.', 799.00, 'Xiaomi', 2025,
        'XIA-15P-001', 45, 'IN_STOCK',
        'Snapdragon 8 Elite', 8, '6.73"', '32 MP', '50-50-50 MP', '6100 mAh',
        NOW() - INTERVAL '2 days', NULL),
       (3, 'Google Pixel 10 Pro',
        'Google flagship phone with pure Android and AI camera features.', 899.00, 'Google', 2026,
        'GOO-P10P-001', 40, 'IN_STOCK',
        'Google Tensor G5', 8, '6.7"', '42 MP', '50-48-48 MP', '5000 mAh',
        NOW() - INTERVAL '1 day', NULL)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    price = EXCLUDED.price,
    brand = EXCLUDED.brand,
    release_year = EXCLUDED.release_year,
    sku = EXCLUDED.sku,
    stock = EXCLUDED.stock,
    status = EXCLUDED.status,
    cpu = EXCLUDED.cpu,
    cores_number = EXCLUDED.cores_number,
    screen_size = EXCLUDED.screen_size,
    front_camera = EXCLUDED.front_camera,
    main_camera = EXCLUDED.main_camera,
    battery_capacity = EXCLUDED.battery_capacity,
    created_at = EXCLUDED.created_at,
    updated_at = NOW();

INSERT INTO phone_colors (phone_id, color)
VALUES (1, 'BLACK'),
       (1, 'WHITE'),
       (1, 'GOLD'),
       (1, 'BLUE'),
       (2, 'WHITE'),
       (2, 'GREEN'),
       (2, 'BLACK'),
       (3, 'BLACK'),
       (3, 'WHITE'),
       (3, 'BLUE')
ON CONFLICT (phone_id, color) DO NOTHING;

INSERT INTO phone_storages (phone_id, storage)
VALUES (1, 'CAPACITY_256GB'),
       (1, 'CAPACITY_512GB'),
       (1, 'CAPACITY_1TB'),
       (2, 'CAPACITY_256GB'),
       (2, 'CAPACITY_512GB'),
       (3, 'CAPACITY_128GB'),
       (3, 'CAPACITY_256GB'),
       (3, 'CAPACITY_512GB')
ON CONFLICT (phone_id, storage) DO NOTHING;

WITH variant_options AS (
    SELECT p.id AS phone_id,
           p.sku AS phone_sku,
           p.price,
           p.stock AS phone_stock,
           pc.color,
           ps.storage AS storage_capacity,
           ROW_NUMBER() OVER (PARTITION BY p.id ORDER BY pc.color, ps.storage) AS row_number,
           COUNT(*) OVER (PARTITION BY p.id) AS variant_count
    FROM phones p
    JOIN phone_colors pc ON pc.phone_id = p.id
    JOIN phone_storages ps ON ps.phone_id = p.id
    WHERE p.id IN (1, 2, 3)
),
variant_stock AS (
    SELECT *,
           (phone_stock / variant_count)
               + CASE WHEN row_number <= MOD(phone_stock, variant_count) THEN 1 ELSE 0 END AS stock
    FROM variant_options
)
INSERT INTO product_variants (fk_phone_id, sku, color, storage_capacity, price, stock, status)
SELECT phone_id,
       LEFT(phone_sku || '-' || color || '-' || REPLACE(storage_capacity, 'CAPACITY_', ''), 64),
       color,
       storage_capacity,
       price,
       stock,
       CASE
           WHEN stock = 0 THEN 'OUT_OF_STOCK'
           WHEN stock <= 9 THEN 'LOW_STOCK'
           ELSE 'IN_STOCK'
       END
FROM variant_stock
ON CONFLICT (fk_phone_id, color, storage_capacity) DO UPDATE
SET sku = EXCLUDED.sku,
    price = EXCLUDED.price,
    stock = EXCLUDED.stock,
    status = EXCLUDED.status,
    updated_at = NOW();

SELECT setval(pg_get_serial_sequence('phones', 'id'), GREATEST((SELECT COALESCE(MAX(id), 1) FROM phones), 3));
