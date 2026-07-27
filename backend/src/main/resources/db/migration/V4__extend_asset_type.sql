-- Extend asset_type column to accommodate new enum values (ETF, FUND, CRYPTO)
ALTER TABLE positions MODIFY COLUMN asset_type VARCHAR(10) NOT NULL;
-- Note: VARCHAR(10) already fits all current values; this migration documents the intent.
-- If a longer value is ever added, increase the size here and in the entity column definition.

