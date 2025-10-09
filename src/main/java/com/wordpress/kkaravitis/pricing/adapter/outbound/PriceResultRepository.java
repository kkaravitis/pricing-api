/*
 * Copyright (c) 2025 Konstantinos Karavitis
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 * You may not use this file for commercial purposes.
 * See the LICENSE file in the project root or visit:
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package com.wordpress.kkaravitis.pricing.adapter.outbound;

import com.wordpress.kkaravitis.pricing.domain.PricingResult;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.wordpress.kkaravitis.pricing.jooq.Tables.PRODUCT_PRICE_RESULTS;

@Repository
public class PriceResultRepository {

    private final DSLContext dsl;

    public PriceResultRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void save(PricingResult result) {
        dsl.insertInto(PRODUCT_PRICE_RESULTS)
              .set(PRODUCT_PRICE_RESULTS.PRODUCT_ID, result.getProductId())
              .set(PRODUCT_PRICE_RESULTS.PRODUCT_NAME, result.getProductName())
              .set(PRODUCT_PRICE_RESULTS.PRICE, result.getPrice())
              .set(PRODUCT_PRICE_RESULTS.CURRENCY, result.getCurrency())
              .set(PRODUCT_PRICE_RESULTS.TIMESTAMP, result.getTimestamp())
              .set(PRODUCT_PRICE_RESULTS.DEMAND_METRIC, result.getDemandMetric())
              .set(PRODUCT_PRICE_RESULTS.COMPETITOR_PRICE, result.getCompetitorPrice())
              .set(PRODUCT_PRICE_RESULTS.INVENTORY_LEVEL, result.getInventoryLevel())
              .set(PRODUCT_PRICE_RESULTS.MODEL_PREDICTION, result.getModelPrediction())
              .onConflict(PRODUCT_PRICE_RESULTS.PRODUCT_ID,
                    PRODUCT_PRICE_RESULTS.TIMESTAMP)
              .doNothing()
              .execute();
    }
}

