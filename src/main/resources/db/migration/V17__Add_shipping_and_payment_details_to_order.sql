ALTER TABLE orders
    DROP COLUMN payment_status,
    ADD COLUMN apartment_number     VARCHAR(100),
    ADD COLUMN house_number         VARCHAR(100) NOT NULL,
    ADD COLUMN logistics_company    VARCHAR(100),
    ADD COLUMN logistic_post_office VARCHAR(100),
    ADD COLUMN tracking_number      VARCHAR(100),
    ADD COLUMN street               VARCHAR(100) NOT NULL,
    ADD COLUMN city                 VARCHAR(100) NOT NULL,
    ADD COLUMN region               VARCHAR(100) NOT NULL,
    ADD COLUMN country              VARCHAR(100) NOT NULL,
    ADD COLUMN zip_code             VARCHAR(100) NOT NULL,
    ADD COLUMN payment_status       VARCHAR(100) NOT NULL,
    ADD COLUMN payment_id           VARCHAR(100);

ALTER TABLE orders
    ADD CONSTRAINT CK_orders_payment_status_valid CHECK (
        payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')

        ),
    ADD CONSTRAINT CK_orders_logistics_company_valid CHECK (
        logistics_company IN ('NOVA_POSHTA', 'UKR_POSHTA', 'DHL')
        )
