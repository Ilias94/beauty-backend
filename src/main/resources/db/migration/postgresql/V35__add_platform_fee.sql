-- Overrides the platform's default fee for one instructor, e.g. 0 for founding instructors.
ALTER TABLE users ADD COLUMN platform_fee_percent decimal(5, 2);

-- What the platform kept and which instructor account the rest went to.
ALTER TABLE payment
    ADD COLUMN platform_fee decimal(38, 2),
    ADD COLUMN destination_account varchar(255);
