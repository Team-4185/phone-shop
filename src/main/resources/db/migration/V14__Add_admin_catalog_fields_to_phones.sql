ALTER TABLE phones
    ADD COLUMN sku VARCHAR(64),
    ADD COLUMN stock INT,
    ADD COLUMN status VARCHAR(20);

UPDATE phones
SET sku    = 'PHONE-' || id,
    stock  = 0,
    status = 'IN_STOCK'
WHERE sku IS NULL
   OR stock IS NULL
   OR status IS NULL;

ALTER TABLE phones
    ALTER COLUMN sku SET NOT NULL,
    ALTER COLUMN stock SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE phones
    ADD CONSTRAINT uk_phones_sku UNIQUE (sku),
    ADD CONSTRAINT ck_phones_stock_non_negative CHECK (stock >= 0),
    ADD CONSTRAINT ck_phones_status_valid CHECK (status IN ('IN_STOCK','LOW_STOCK','OUT_OF_STOCK'));

