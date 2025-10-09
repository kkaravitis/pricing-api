/*
 * Copyright (c) 2025 Konstantinos Karavitis
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 * You may not use this file for commercial purposes.
 * See the LICENSE file in the project root or visit:
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package com.wordpress.kkaravitis.pricing.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductPriceResultWithChange {
    private Long id;
    private String productId;
    private String productName;
    private BigDecimal price;
    private String currency;
    private LocalDateTime timestamp;
    private Double demandMetric;
    private Double competitorPrice;
    private Double inventoryLevel;
    private Double modelPrediction;

    private BigDecimal previousPrice;
    private BigDecimal priceChangePercent;
    private String priceChangeLabel;
}
