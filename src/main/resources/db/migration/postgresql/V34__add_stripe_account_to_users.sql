-- An instructor's Stripe Connect account, which course payments are paid out to.
ALTER TABLE users
    ADD COLUMN stripe_account_id varchar(255),
    ADD COLUMN stripe_details_submitted boolean NOT NULL DEFAULT false,
    ADD COLUMN stripe_charges_enabled boolean NOT NULL DEFAULT false,
    ADD COLUMN stripe_payouts_enabled boolean NOT NULL DEFAULT false;

-- account.updated webhooks look the instructor up by account id.
CREATE UNIQUE INDEX idx_users_stripe_account_id ON users (stripe_account_id);
