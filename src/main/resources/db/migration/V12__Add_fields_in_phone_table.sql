ALTER TABLE phones
    ADD COLUMN cpu            VARCHAR(50),
    ADD COLUMN cores_number   INT,
    ADD COLUMN screen_size    VARCHAR(5),
    ADD COLUMN front_camera   VARCHAR(10),
    ADD COLUMN main_camera    VARCHAR(20),
    ADD COLUMN battery_capacity VARCHAR(10);

UPDATE phones SET
                  cpu = 'Unknown', cores_number = 0, screen_size = '0',
                  front_camera = '0 MP', main_camera = '0 MP', battery_capacity = '0 mAh'
WHERE cpu IS NULL;

ALTER TABLE phones
    ALTER COLUMN cpu            SET NOT NULL,
    ALTER COLUMN cores_number   SET NOT NULL,
    ALTER COLUMN screen_size    SET NOT NULL,
    ALTER COLUMN front_camera   SET NOT NULL,
    ALTER COLUMN main_camera    SET NOT NULL,
    ALTER COLUMN battery_capacity SET NOT NULL;