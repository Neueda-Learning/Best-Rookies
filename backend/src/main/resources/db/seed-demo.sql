-- Demo seed data for local development and manual testing.
-- Run this after Flyway migrations have created the schema.
--
-- Example:
--   mysql -u root -p portfolio_db < src/main/resources/db/seed-demo.sql

INSERT INTO portfolios (name, base_currency, created_at)
VALUES ('Demo Growth Portfolio', 'USD', CURRENT_TIMESTAMP());

INSERT INTO portfolios (name, base_currency, created_at)
VALUES ('Income Portfolio', 'USD', CURRENT_TIMESTAMP());

INSERT INTO positions (portfolio_id, asset_type, ticker, quantity, avg_cost, currency, updated_at)
SELECT p.id, 'STOCK', 'AAPL', 12.0000, 150.0000, 'USD', CURRENT_TIMESTAMP()
FROM portfolios p
WHERE p.name = 'Demo Growth Portfolio';

INSERT INTO positions (portfolio_id, asset_type, ticker, quantity, avg_cost, currency, updated_at)
SELECT p.id, 'ETF', 'VOO', 4.0000, 430.0000, 'USD', CURRENT_TIMESTAMP()
FROM portfolios p
WHERE p.name = 'Demo Growth Portfolio';

INSERT INTO positions (portfolio_id, asset_type, ticker, quantity, avg_cost, currency, updated_at)
SELECT p.id, 'BOND', 'BND', 20.0000, 72.5000, 'USD', CURRENT_TIMESTAMP()
FROM portfolios p
WHERE p.name = 'Income Portfolio';

