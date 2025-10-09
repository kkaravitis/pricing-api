CREATE TABLE product_price_results (
    id BIGSERIAL PRIMARY KEY,

    product_id VARCHAR(64) NOT NULL,
    product_name VARCHAR(255),

    price NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'EUR',
    timestamp TIMESTAMP NOT NULL,

    demand_metric DOUBLE PRECISION,
    competitor_price DOUBLE PRECISION,
    inventory_level DOUBLE PRECISION,
    model_prediction DOUBLE PRECISION,

    -- Soft rule: Avoid same timestamp per product per tenant
    CONSTRAINT uq_product_time UNIQUE (product_id, timestamp)
);

-- Index for fast lookup by tenant and product over time
CREATE INDEX idx_product_time
    ON product_price_results (product_id, timestamp DESC);