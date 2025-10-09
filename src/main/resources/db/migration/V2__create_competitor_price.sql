CREATE TABLE IF NOT EXISTS competitor_price (
  id                BIGSERIAL PRIMARY KEY,
  product_id        VARCHAR(128) NOT NULL UNIQUE,
  product_name      VARCHAR(255),
  competitor_price  NUMERIC(10,2) NOT NULL,
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS ix_competitor_price_updated
  ON competitor_price(updated_at DESC);