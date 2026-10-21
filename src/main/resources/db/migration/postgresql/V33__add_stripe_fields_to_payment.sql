ALTER TABLE payment
    ADD COLUMN amount decimal(38, 2),
    ADD COLUMN currency varchar(3),
    ADD COLUMN stripe_session_id varchar(255),
    ADD COLUMN stripe_payment_intent_id varchar(255);

-- Webhooks look payments up by order id.
CREATE UNIQUE INDEX idx_payment_order_id ON payment (order_id);
