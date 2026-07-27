-- Improve query performance for positions filtered by portfolio
CREATE INDEX idx_positions_portfolio_id ON positions (portfolio_id);

-- Improve lookup performance when querying by ticker symbol
CREATE INDEX idx_positions_ticker ON positions (ticker);

