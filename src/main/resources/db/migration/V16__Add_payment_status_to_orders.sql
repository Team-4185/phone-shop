ALTER TABLE orders
    ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

ALTER TABLE orders
    ADD CONSTRAINT CK_orders_payment_status_valid CHECK (
        payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')
    );

ALTER TABLE orders
    ALTER COLUMN payment_status DROP DEFAULT;
