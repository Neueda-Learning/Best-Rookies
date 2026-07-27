-- 创建组合表
CREATE TABLE portfolios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    base_currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- 创建持仓表
CREATE TABLE positions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    portfolio_id BIGINT NOT NULL,
    asset_type VARCHAR(10) NOT NULL,
    ticker VARCHAR(12) NOT NULL,
    quantity DECIMAL(18, 4) NOT NULL,
    avg_cost DECIMAL(18, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_positions_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id)
);
