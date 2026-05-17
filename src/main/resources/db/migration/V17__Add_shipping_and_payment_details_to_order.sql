ALTER TABLE orders
    ADD COLUMN apartment_number     VARCHAR(100),
    ADD COLUMN house_number         VARCHAR(100),
    ADD COLUMN logistics_company    VARCHAR(100),
    ADD COLUMN logistic_post_office VARCHAR(100),
    ADD COLUMN tracking_number      VARCHAR(100),
    ADD COLUMN street               VARCHAR(100),
    ADD COLUMN city                 VARCHAR(100),
    ADD COLUMN region               VARCHAR(100),
    ADD COLUMN country              VARCHAR(100),
    ADD COLUMN zip_code             VARCHAR(100),
    ADD COLUMN transaction_id       VARCHAR(100),
    DROP COLUMN customer_city;

ALTER TABLE orders
    ADD CONSTRAINT CK_orders_logistics_company_valid CHECK (
        logistics_company IN ('NOVA_POSHTA', 'UKR_POSHTA', 'DHL')
        )