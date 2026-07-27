-- Store market price snapshots fetched from external price sources (e.g. Yahoo Finance)
CREATE TABLE price_snapshots (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticker      VARCHAR(12)    NOT NULL,
    price       DECIMAL(18, 4) NOT NULL,
    currency    VARCHAR(3)     NOT NULL,
    fetched_at  TIMESTAMP      NOT NULL,
    INDEX idx_price_snapshots_ticker (ticker),
    INDEX idx_price_snapshots_fetched_at (fetched_at)
);

