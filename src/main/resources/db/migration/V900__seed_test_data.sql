-- roles
INSERT INTO roles (id, name, created_at, updated_at)
    OVERRIDING SYSTEM VALUE
VALUES (1, 'ROLE_ADMIN', NOW(), NULL),
       (2, 'ROLE_USER', NOW(), NULL)
ON CONFLICT (id) DO NOTHING;

-- users pass: asdASD1!
INSERT INTO users (id, email, password, first_name, last_name, city, phone_number, fk_role_id, created_at, updated_at)
    OVERRIDING SYSTEM VALUE
VALUES (1, 'admin@email.com', '$2a$10$uBadJkAdWOox4Dtdwv65XOpUFJdL0y6eiboIHaJ5do38RTMBfrLdW', 'Admin', 'Admin', NULL,
        NULL, 1, NOW(), NULL),
       (2, 'us1@email.com', '$2a$10$uBadJkAdWOox4Dtdwv65XOpUFJdL0y6eiboIHaJ5do38RTMBfrLdW', 'User1', 'User1', 'Kiev',
        NULL, 2, NOW(), NULL),
       (3, 'us2@email.com', '$2a$10$uBadJkAdWOox4Dtdwv65XOpUFJdL0y6eiboIHaJ5do38RTMBfrLdW', 'User2', 'User2', NULL,
        NULL, 2, NOW(), NULL),
       (4, 'us3@email.com', '$2a$10$uBadJkAdWOox4Dtdwv65XOpUFJdL0y6eiboIHaJ5do38RTMBfrLdW', 'User3', 'User3', 'Lviv',
        NULL, 2, NOW(), NULL)
ON CONFLICT (id) DO NOTHING;

-- phones
INSERT INTO phones (id, name, description, price, brand, release_year, sku, stock, status,
                    cpu, cores_number, screen_size, front_camera, main_camera, battery_capacity,
                    created_at, updated_at)
    OVERRIDING SYSTEM VALUE
VALUES (1, 'iPhone 15 Pro', 'Flagship Apple smartphone', 999.99, 'Apple', 2023, 'APL-IP15P-001', 50, 'IN_STOCK',
        'Apple A17 Pro', 6, '6.1"', '12MP', '48MP+12MP+12MP', '3274mAh', NOW(), NULL),
       (2, 'iPhone 15', 'Standard Apple smartphone', 799.99, 'Apple', 2023, 'APL-IP15-001', 80, 'IN_STOCK', 'Apple A16',
        6, '6.1"', '12MP', '48MP+12MP', '3877mAh', NOW(), NULL),
       (3, 'iPhone 14 Pro Max', 'Previous gen Apple flagship', 899.99, 'Apple', 2022, 'APL-IP14PM-001', 30, 'LOW_STOCK',
        'Apple A15 Pro', 6, '6.7"', '12MP', '48MP+12MP+12MP', '4323mAh', NOW(), NULL),
       (4, 'iPhone 13', 'Older Apple model', 599.99, 'Apple', 2021, 'APL-IP13-001', 10, 'LOW_STOCK', 'Apple A15', 6,
        '6.1"', '12MP', '12MP+12MP', '3227mAh', NOW(), NULL),
       (5, 'Samsung Galaxy S24 Ultra', 'Top Samsung flagship', 1199.99, 'Samsung', 2024, 'SAM-S24U-001', 60, 'IN_STOCK',
        'Snapdragon 8 Gen 3', 8, '6.8"', '12MP', '200MP+12MP+10MP+10MP', '5000mAh', NOW(), NULL),
       (6, 'Samsung Galaxy S24', 'Standard Samsung flagship', 799.99, 'Samsung', 2024, 'SAM-S24-001', 70, 'IN_STOCK',
        'Snapdragon 8 Gen 3', 8, '6.2"', '12MP', '50MP+12MP+10MP', '4000mAh', NOW(), NULL),
       (7, 'Samsung Galaxy A55', 'Mid-range Samsung', 449.99, 'Samsung', 2024, 'SAM-A55-001', 90, 'IN_STOCK',
        'Exynos 1480', 8, '6.6"', '32MP', '50MP+12MP+5MP', '5000mAh', NOW(), NULL),
       (8, 'Samsung Galaxy A35', 'Budget Samsung', 349.99, 'Samsung', 2024, 'SAM-A35-001', 5, 'LOW_STOCK',
        'Exynos 1380', 8, '6.6"', '13MP', '50MP+8MP+5MP', '5000mAh', NOW(), NULL),
       (9, 'Samsung Galaxy S23', 'Previous gen Samsung', 649.99, 'Samsung', 2023, 'SAM-S23-001', 0, 'OUT_OF_STOCK',
        'Snapdragon 8 Gen 2', 8, '6.1"', '12MP', '50MP+12MP+10MP', '3900mAh', NOW(), NULL),
       (10, 'Google Pixel 8 Pro', 'Top Google flagship', 999.99, 'Google', 2023, 'GOO-P8P-001', 40, 'IN_STOCK',
        'Google Tensor G3', 8, '6.7"', '10.5MP', '50MP+48MP+48MP', '5050mAh', NOW(), NULL),
       (11, 'Google Pixel 8', 'Standard Google flagship', 699.99, 'Google', 2023, 'GOO-P8-001', 55, 'IN_STOCK',
        'Google Tensor G3', 8, '6.2"', '10.5MP', '50MP+12MP', '4575mAh', NOW(), NULL),
       (12, 'Google Pixel 7a', 'Mid-range Google', 499.99, 'Google', 2023, 'GOO-P7A-001', 20, 'LOW_STOCK',
        'Google Tensor G2', 8, '6.1"', '13MP', '64MP+13MP', '4385mAh', NOW(), NULL),
       (13, 'Xiaomi 14 Pro', 'Top Xiaomi flagship', 899.99, 'Xiaomi', 2024, 'XIA-14P-001', 35, 'IN_STOCK',
        'Snapdragon 8 Gen 3', 8, '6.73"', '32MP', '50MP+50MP+50MP', '4880mAh', NOW(), NULL),
       (14, 'Xiaomi 14', 'Standard Xiaomi flagship', 699.99, 'Xiaomi', 2024, 'XIA-14-001', 45, 'IN_STOCK',
        'Snapdragon 8 Gen 3', 8, '6.36"', '32MP', '50MP+50MP+50MP', '4610mAh', NOW(), NULL),
       (15, 'Xiaomi Redmi Note 13', 'Budget Xiaomi', 249.99, 'Xiaomi', 2024, 'XIA-RN13-001', 100, 'IN_STOCK',
        'Snapdragon 685', 8, '6.67"', '16MP', '108MP+8MP+2MP', '5000mAh', NOW(), NULL),
       (16, 'OnePlus 12', 'OnePlus flagship', 799.99, 'OnePlus', 2024, 'ONE-12-001', 30, 'IN_STOCK',
        'Snapdragon 8 Gen 3', 8, '6.82"', '32MP', '50MP+48MP+64MP', '5400mAh', NOW(), NULL),
       (17, 'OnePlus 12R', 'OnePlus mid-range', 499.99, 'OnePlus', 2024, 'ONE-12R-001', 25, 'IN_STOCK',
        'Snapdragon 8 Gen 1', 8, '6.78"', '16MP', '50MP+8MP+2MP', '5500mAh', NOW(), NULL),
       (18, 'Sony Xperia 1 VI', 'Sony flagship', 1299.99, 'Sony', 2024, 'SON-X1VI-001', 15, 'LOW_STOCK',
        'Snapdragon 8 Gen 3', 8, '6.5"', '12MP', '52MP+12MP+12MP', '5000mAh', NOW(), NULL),
       (19, 'Sony Xperia 5 V', 'Compact Sony flagship', 999.99, 'Sony', 2023, 'SON-X5V-001', 8, 'LOW_STOCK',
        'Snapdragon 8 Gen 2', 8, '6.1"', '12MP', '48MP+12MP', '5000mAh', NOW(), NULL),
       (20, 'Motorola Edge 50 Pro', 'Motorola flagship', 599.99, 'Motorola', 2024, 'MOT-E50P-001', 40, 'IN_STOCK',
        'Snapdragon 7 Gen 3', 8, '6.7"', '50MP', '50MP+10MP+13MP', '4500mAh', NOW(), NULL),
       (21, 'Motorola Moto G84', 'Budget Motorola', 299.99, 'Motorola', 2023, 'MOT-G84-001', 60, 'IN_STOCK',
        'Snapdragon 695', 8, '6.55"', '16MP', '50MP+8MP', '5000mAh', NOW(), NULL),
       (22, 'Huawei Pura 70 Pro', 'Huawei flagship', 999.99, 'Huawei', 2024, 'HUA-P70P-001', 20, 'IN_STOCK',
        'Kirin 9010', 8, '6.8"', '13MP', '50MP+48MP+40MP', '5100mAh', NOW(), NULL),
       (23, 'Nothing Phone 2', 'Unique design smartphone', 599.99, 'Nothing', 2023, 'NOT-P2-001', 35, 'IN_STOCK',
        'Snapdragon 8+ Gen 1', 8, '6.7"', '32MP', '50MP+50MP', '4700mAh', NOW(), NULL),
       (24, 'Asus Zenfone 11 Ultra', 'Asus flagship', 899.99, 'Asus', 2024, 'ASU-ZF11U-001', 18, 'LOW_STOCK',
        'Snapdragon 8 Gen 3', 8, '6.78"', '32MP', '50MP+13MP+32MP', '5800mAh', NOW(), NULL),
       (25, 'Realme GT 6', 'Realme flagship', 599.99, 'Realme', 2024, 'REA-GT6-001', 50, 'IN_STOCK',
        'Snapdragon 8s Gen 3', 8, '6.78"', '32MP', '50MP+8MP+2MP', '5500mAh', NOW(), NULL)
ON CONFLICT (id) DO NOTHING;

-- orders
INSERT INTO orders (id, fk_user_id, customer_email, customer_first_name, customer_last_name, customer_phone_number,
                    status, payment_method, delivery_method,
                    apartment_number, house_number, logistics_company, logistic_post_office, tracking_number,
                    street, city, region, country, zip_code,
                    payment_status, transaction_id,
                    total, created_at, updated_at)
    OVERRIDING SYSTEM VALUE
VALUES
-- anon CARD, COURIER, DELIVERED, PAID
(1, NULL, 'anon1@email.com', 'John', 'Doe', '+380991234567', 'DELIVERED', 'CARD', 'COURIER', '12A', '25', 'DHL', NULL,
 'TRK-001', 'Khreshchatyk St', 'Kyiv', 'Kyiv Oblast', 'Ukraine', '01001', 'PAID', 'TXN-001', 999.99,
 NOW() - INTERVAL '30 days', NOW() - INTERVAL '25 days'),
-- anon, CASH_ON_DELIVERY, POST_OFFICE, CANCELLED, PENDING
(2, NULL, 'anon2@email.com', 'Anna', 'Smith', '+380992345678', 'CANCELLED', 'CASH_ON_DELIVERY', 'POST_OFFICE', NULL,
 '10', 'NOVA_POSHTA', 'NP #5 Lviv', NULL, 'Svobody Ave', 'Lviv', 'Lviv Oblast', 'Ukraine', '79000', 'PENDING', NULL,
 449.99, NOW() - INTERVAL '20 days', NOW() - INTERVAL '18 days'),
-- anon, CARD, PICKUP, PROCESSING, PAID
(3, NULL, 'anon3@email.com', 'Mike', 'Brown', '+380993456789', 'PROCESSING', 'CARD', 'PICKUP', NULL, NULL, NULL, NULL,
 'TRK-003', NULL, NULL,NULL,NULl, NULL, 'PAID', 'TXN-003', 1199.99,
 NOW() - INTERVAL '5 days', NULL),
-- us1, CARD, COURIER, NEW, PENDING
(4, 2, 'us1@email.com', 'User1', 'User1', '+380994567890', 'NEW', 'CARD', 'COURIER', '5B', '33', 'NOVA_POSHTA', NULL, NULL,
 'Derybasivska St', 'Odesa', 'Odesa Oblast', 'Ukraine', '65000', 'PENDING', NULL, 799.99, NOW() - INTERVAL '1 day',
 NULL),
-- us1, CASH_ON_DELIVERY, POST_OFFICE, SHIPPED, PENDING
(5, 2, 'us1@email.com', 'User1', 'User1', '+380994567890', 'SHIPPED', 'CASH_ON_DELIVERY', 'POST_OFFICE', NULL, NULL,
 'UKR_POSHTA', 'UP #12 Dnipro', 'TRK-005', NULL, 'Dnipro', 'Dnipro Oblast', 'Ukraine', '49000', 'PENDING',
 NULL, 249.99, NOW() - INTERVAL '10 days', NOW() - INTERVAL '7 days'),
-- us2, CARD, COURIER, CONFIRMED, PAID
(6, 3, 'us2@email.com', 'User2', 'User2', '+380995678901', 'CONFIRMED', 'CARD', 'COURIER', '3', '15', 'DHL', NULL, NULL,
 'Shevchenko Blvd', 'Zaporizhzhia', 'Zaporizhzhia Oblast', 'Ukraine', '69000', 'PAID', 'TXN-006', 1799.98,
 NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days'),
-- us2, CARD, PICKUP, DELIVERED, PAID
(7, 3, 'us2@email.com', 'User2', 'User2', '+380995678901', 'DELIVERED', 'CARD', 'PICKUP', NULL, NULL, NULL, NULL,
 'TRK-007', NULL, NULL, NULL, NULL, NULL, 'PAID', 'TXN-007', 599.99,
 NOW() - INTERVAL '45 days', NOW() - INTERVAL '40 days'),
-- us3, CASH_ON_DELIVERY, POST_OFFICE, DELIVERED, PAID
(8, 4, 'us3@email.com', 'User3', 'User3', '+380996789012', 'DELIVERED', 'CASH_ON_DELIVERY', 'POST_OFFICE', NULL, '88',
 'NOVA_POSHTA', 'NP #3 Kharkiv', 'TRK-008', NULL, 'Kharkiv', 'Kharkiv Oblast', 'Ukraine', '61022', 'PAID', NULL,
 899.99, NOW() - INTERVAL '60 days', NOW() - INTERVAL '55 days'),
-- us3, CARD, COURIER, PROCESSING, FAILED
(9, 4, 'us3@email.com', 'User3', 'User3', '+380996789012', 'PROCESSING', 'CARD', 'COURIER', '1A', '4', 'DHL', NULL, NULL,
 'Lesi Ukrainky Blvd', 'Kyiv', 'Kyiv Oblast', 'Ukraine', '01133', 'FAILED', 'TXN-009', 1299.99,
 NOW() - INTERVAL '2 days', NULL),
-- anon, CARD, POST_OFFICE, NEW, PENDING
(10, NULL, 'anon4@email.com', 'Kate', 'Wilson', '+380997890123', 'NEW', 'CARD', 'POST_OFFICE', NULL, '19',
 'NOVA_POSHTA', 'NP #7 Odesa', NULL, 'Velyka Arnautska', 'Odesa', 'Odesa Oblast', 'Ukraine', '65012', 'PENDING', NULL,
 699.99, NOW(), NULL)
ON CONFLICT (id) DO NOTHING;

-- orders_items
INSERT INTO orders_items (id, fk_order_id, fk_phone_id, product_name, sku, unit_price, quantity, total_price,
                          created_at, updated_at)
    OVERRIDING SYSTEM VALUE
VALUES
-- order 1: iPhone 15 Pro x1
(1, 1, 1, 'iPhone 15 Pro', 'APL-IP15P-001', 999.99, 1, 999.99, NOW() - INTERVAL '30 days', NULL),
-- order 2: Samsung Galaxy A55 x1
(2, 2, 7, 'Samsung Galaxy A55', 'SAM-A55-001', 449.99, 1, 449.99, NOW() - INTERVAL '20 days', NULL),
-- order 3: Samsung Galaxy S24 Ultra x1
(3, 3, 5, 'Samsung Galaxy S24 Ultra', 'SAM-S24U-001', 1199.99, 1, 1199.99, NOW() - INTERVAL '5 days', NULL),
-- order 4: iPhone 15 x1
(4, 4, 2, 'iPhone 15', 'APL-IP15-001', 799.99, 1, 799.99, NOW() - INTERVAL '1 day', NULL),
-- order 5: Xiaomi Redmi Note 13 x1
(5, 5, 15, 'Xiaomi Redmi Note 13', 'XIA-RN13-001', 249.99, 1, 249.99, NOW() - INTERVAL '10 days', NULL),
-- order 6: Google Pixel 8 Pro x1 + Xiaomi 14 x1
(6, 6, 10, 'Google Pixel 8 Pro', 'GOO-P8P-001', 999.99, 1, 999.99, NOW() - INTERVAL '3 days', NULL),
(7, 6, 14, 'Xiaomi 14', 'XIA-14-001', 699.99, 1, 699.99, NOW() - INTERVAL '3 days', NULL),
-- order 7: Sony Xperia 5 V x1
(8, 7, 19, 'Sony Xperia 5 V', 'SON-X5V-001', 599.99, 1, 599.99, NOW() - INTERVAL '45 days', NULL),
-- order 8: iPhone 14 Pro Max x1
(9, 8, 3, 'iPhone 14 Pro Max', 'APL-IP14PM-001', 899.99, 1, 899.99, NOW() - INTERVAL '60 days', NULL),
-- order 9: Sony Xperia 1 VI x1
(10, 9, 18, 'Sony Xperia 1 VI', 'SON-X1VI-001', 1299.99, 1, 1299.99, NOW() - INTERVAL '2 days', NULL),
-- order 10: Samsung Galaxy S24 x1
(11, 10, 6, 'Samsung Galaxy S24', 'SAM-S24-001', 699.99, 1, 699.99, NOW(), NULL)
ON CONFLICT (id) DO NOTHING;
-- set sequences
SELECT setval(pg_get_serial_sequence('roles', 'id'), (SELECT MAX(id) FROM roles));
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('phones', 'id'), (SELECT MAX(id) FROM phones));
SELECT setval(pg_get_serial_sequence('orders', 'id'), (SELECT MAX(id) FROM orders));
SELECT setval(pg_get_serial_sequence('orders_items', 'id'), (SELECT MAX(id) FROM orders_items));